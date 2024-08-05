package com.kukuxer.tgBotQrCode;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeRepository;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeService;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import com.kukuxer.tgBotQrCode.user.UserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;


import java.awt.*;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Random;


@Component
@Slf4j
public class QRCodeTgBot extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;
    private final QRCodeGenerator qrCodeGenerator;
    private final UserService userService;
    private final TgBotUtils tgBotUtils;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;
    private final QrCodeRepository qrCodeRepository;
    private static final Random random = new Random();

    public QRCodeTgBot(
            TelegramBotConfig telegramBotConfig,
            QRCodeGenerator qrCodeGenerator,
            UserService userService, TgBotUtils tgBotUtils, UserRepository userRepository, QrCodeService qrCodeService, QrCodeRepository qrCodeRepository
    ) {
        this.telegramBotConfig = telegramBotConfig;
        this.qrCodeGenerator = qrCodeGenerator;
        this.userService = userService;
        this.tgBotUtils = tgBotUtils;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.qrCodeRepository = qrCodeRepository;
        generateMenuButtons();
    }

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return telegramBotConfig.getBotToken();
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        TgUser user = userService.getByChatIdOrElseCreateNew(update);

        if (isCommand(update)) {
            String text = update.getMessage().getText();
            processCommands(user, text);
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            processCallbackQuery(user, callbackQuery);
        }
        if (user.getStepOfGenerationCode() == 2 && update.hasMessage()) {
            setCustomColorForForeGround(user, update);
        } else if (user.getStepOfGenerationCode() == 3 && update.hasMessage()) {
            setCustomColorForBackGround(user, update);
        }

        if (user.isOnFinalStepOfCreation() && update.hasMessage()) {
            generateQrCode(user, update);
        }
    }

    private void setCustomColorForForeGround(TgUser user, Update update) {
        try {
            QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(() -> new RuntimeException("QR Code not found for user"));
            qrCode.setForegroundColor(Color.decode(update.getMessage().getText()));
            qrCodeRepository.save(qrCode);
            showUserPossibleCustomizationForBackGround(user);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessageToUser(user, "something went wrong, try again(probably wrong format)");
        }
    }

    private void setCustomColorForBackGround(TgUser user, Update update) {
        try {
            QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(() -> new RuntimeException("QR Code not found for user"));
            qrCode.setBackgroundColor(Color.decode(update.getMessage().getText()));
            qrCodeRepository.save(qrCode);
            tellUserToWriteTextForQRCode(user);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessageToUser(user, "something went wrong, try again(probably wrong format)");
        }
    }

    @SneakyThrows
    private void generateQrCode(TgUser user, Update update) {
        QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(() -> new RuntimeException("QR Code not found for user"));
        String text = update.getMessage().getText();
        String fullLink = "http://localhost:8080/redirect/" + qrCode.getUuid();
        log.info("link: " + fullLink);
        InputFile qrFile = qrCodeGenerator.getQRCodeImageFile(fullLink, qrCode.getForegroundColor(), qrCode.getBackgroundColor());
        sendPhoto(update.getMessage().getChatId(), qrFile);

        qrCode.setIsCreated(true);
        qrCode.setIsActive(true);
        qrCode.setText(text);
        qrCode.setFullLink(fullLink);
        qrCodeRepository.save(qrCode);
        user.setStepOfGenerationCode(10);
        user.setOnFinalStepOfCreation(false);
        userRepository.save(user);
    }

    @SneakyThrows
    private void sendPhoto(Long chatId, InputFile qrFile) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(qrFile);
        execute(sendPhoto);
    }


    private boolean isCommand(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            return text.startsWith("/");
        }
        return false;
    }

    private void processCommands(TgUser user, String text) {
        switch (text) {
            case "/start" -> sendMessageToUser(
                    user,
                    "Hello, welcome to the bot, that will help you to create your Qr codes!"
            );
            case "/generateqrcode" -> processQRCodeGenerating(user);
        }
    }

    private void processCallbackQuery(TgUser user, CallbackQuery callbackQuery) {
        QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(null);
        switch (callbackQuery.getData()) {
            case "Basic qr code":
                qrCode.setExpirationTime(LocalDateTime.now().plusWeeks(2));
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "Permanent VIP":
                qrCode.setExpirationTime(LocalDateTime.now().plusYears(100));
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForForeground(user);
                break;
            case "Permanent RAW":


                break;
            case "\uD83D\uDFE5Red foreGround ":
                qrCode.setForegroundColor(Color.RED);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE7Orange foreGround ":
                qrCode.setForegroundColor(Color.ORANGE);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE8Yellow foreGround ":
                qrCode.setForegroundColor(Color.YELLOW);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE9Green foreGround ":
                qrCode.setForegroundColor(Color.GREEN);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE6Blue foreGround ":
                qrCode.setForegroundColor(Color.BLUE);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFEAPurple foreGround ":
                qrCode.setForegroundColor(Color.magenta);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFEBBrown foreGround ":
                qrCode.setForegroundColor(new Color(165, 42, 42));
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "⬛\uFE0FBlack foreGround ":
                qrCode.setForegroundColor(Color.BLACK);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "⬜\uFE0FWhite foreGround ":
                qrCode.setForegroundColor(Color.WHITE);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDD33Choose default":
                tellUserToWriteTextForQRCode(user);
                break;
            case "Choose Random color for foreGround":
                qrCode.setForegroundColor(getRandomColor());
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "Create your own color for foreGround", "Create your own color for BackGround":
                askUserToWriteCustomColorInHexadecimalFormat(user);
                break;
            case "\uD83D\uDFE5Red BackGround ":
                qrCode.setBackgroundColor(Color.RED);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE7Orange BackGround ":
                qrCode.setBackgroundColor(Color.ORANGE);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE8Yellow BackGround ":
                qrCode.setBackgroundColor(Color.YELLOW);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE9Green BackGround ":
                qrCode.setBackgroundColor(Color.GREEN);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE6Blue BackGround ":
                qrCode.setBackgroundColor(Color.BLUE);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFEAPurple BackGround ":
                qrCode.setBackgroundColor(Color.magenta);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFEBBrown BackGround ":
                qrCode.setBackgroundColor(new Color(165, 42, 42));
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "⬛\uFE0FBlack BackGround ":
                qrCode.setBackgroundColor(Color.BLACK);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "⬜\uFE0FWhite BackGround ":
                qrCode.setBackgroundColor(Color.WHITE);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "Choose Random color for BackGround":
                qrCode.setBackgroundColor(getRandomColor());
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "⬅\uFE0F back":
                proccesBackButton(user);
                break;
            default:
                // Handle unknown callback data
                break;
        }
    }

    private void proccesBackButton(TgUser user) {
        switch (user.getStepOfGenerationCode()) {
            case 20, 3 -> showUserPossibleCustomizationForForeground(user);
            case 30, 4 -> showUserPossibleCustomizationForBackGround(user);
        }
    }


    @SneakyThrows
    private void showUserPossibleCustomizationForForeground(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            editMessage.setParseMode(ParseMode.MARKDOWNV2);
            editMessage.setText("Choose Color for foreground: ");
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(
                    List.of("\uD83D\uDFE5" + "Red foreGround ",
                            "\uD83D\uDFE7" + "Orange foreGround ",
                            "\uD83D\uDFE8" + "Yellow foreGround ",
                            "\uD83D\uDFE9" + "Green foreGround ",
                            "\uD83D\uDFE6" + "Blue foreGround ",
                            "\uD83D\uDFEA" + "Purple foreGround ",
                            "\uD83D\uDFEB" + "Brown foreGround ",
                            "⬛\uFE0F" + "Black foreGround ",
                            "⬜\uFE0F" + "White foreGround ",
                            "\uD83D\uDD33" + "Choose default",
                            "Choose Random color for foreGround",
                            "Create your own color for foreGround"
                    ));
            editMessage.setReplyMarkup(markup);
            execute(editMessage);
        }
        user.setStepOfGenerationCode(2);
        userRepository.save(user);
    }

    @SneakyThrows
    private void askUserToWriteCustomColorInHexadecimalFormat(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            editMessage.setText("Write your color in Hexadecimal format(e.g., \"#RRGGBB\" or \"RRGGBB\"): \n Example: #FF5733 or FF5733 ");
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅\uFE0F back"));
            editMessage.setReplyMarkup(markup);
            execute(editMessage);
        }
        if (user.getStepOfGenerationCode() == 2) {
            user.setStepOfGenerationCode(20);
            userRepository.save(user);
        } else if (user.getStepOfGenerationCode() == 3) {
            user.setStepOfGenerationCode(30);
            userRepository.save(user);
        }

    }

    @SneakyThrows
    private void showUserPossibleCustomizationForBackGround(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            editMessage.setParseMode(ParseMode.MARKDOWNV2);
            editMessage.setText("Choose Color for BackGround: ");
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(
                    List.of("\uD83D\uDFE5" + "Red BackGround ",
                            "\uD83D\uDFE7" + "Orange BackGround ",
                            "\uD83D\uDFE8" + "Yellow BackGround ",
                            "\uD83D\uDFE9" + "Green BackGround ",
                            "\uD83D\uDFE6" + "Blue BackGround ",
                            "\uD83D\uDFEA" + "Purple BackGround ",
                            "\uD83D\uDFEB" + "Brown BackGround ",
                            "⬛\uFE0F" + "Black BackGround ",
                            "⬜\uFE0F" + "White BackGround ",
                            "Choose Random color for BackGround",
                            "Create your own color for BackGround",
                            "⬅\uFE0F back"
                    ));
            editMessage.setReplyMarkup(markup);
            execute(editMessage);
        }
        user.setStepOfGenerationCode(3);
        userRepository.save(user);
    }

    @SneakyThrows
    private void tellUserToWriteTextForQRCode(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅\uFE0F back"));
            editMessage.setReplyMarkup(markup);
            editMessage.setText("Send link or text for your qr code:");
            execute(editMessage);
        }
        user.setOnFinalStepOfCreation(true);
        user.setStepOfGenerationCode(4);
        userRepository.save(user);
    }

    private void processQRCodeGenerating(TgUser user) {
        if (user.isGenerateQrCodeRightNow()) {
            sendMessageToUser(user, "Sorry but you already generating Qr code!");
            return;
        }
        qrCodeService.generateQrCode(user);
        showOptionsToChooseType(user);
    }


    @SneakyThrows
    public void sendMessageToUser(TgUser user, String text) {
        execute(SendMessage.builder()
                .chatId(user.getChatId().toString())
                .text(text)
                .build()
        );
    }

    @SneakyThrows
    public void generateMenuButtons() {
        List<BotCommand> listOfCommands = List.of(
                new BotCommand("/start", "start to work with bot."),
                new BotCommand("/profile", "open your profile"),
                new BotCommand("/menu", "open bot menu"),
                new BotCommand("/help", "bot usage guide"),
                new BotCommand("/generateqrcode", "create your QR code")
        );
        this.execute(
                new SetMyCommands(
                        listOfCommands,
                        new BotCommandScopeDefault(),
                        null
                )
        );
    }

    public static Color getRandomColor() {
        // Generate random RGB values
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // Create and return a new Color object
        return new Color(red, green, blue);
    }

    @SneakyThrows
    private void showOptionsToChooseType(TgUser user) {

        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        message.setText("Choose a type of a QR code");
        InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("Basic qr code", "Permanent VIP", "Permanent RAW"));
        message.setReplyMarkup(markup);

        Integer messageId = execute(message).getMessageId();
        user.setMessageId(messageId);
        userRepository.save(user);
    }


}


