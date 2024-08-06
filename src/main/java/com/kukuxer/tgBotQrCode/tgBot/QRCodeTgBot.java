package com.kukuxer.tgBotQrCode.tgBot;

import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitor;
import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitorRepository;
import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeRepository;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeService;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import com.kukuxer.tgBotQrCode.user.UserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;


import java.awt.*;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.Random;


@Component
@Slf4j
public class QRCodeTgBot extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;
    private final UserService userService;
    private final TgBotUtils tgBotUtils;
    private final UserRepository userRepository;
    private final QrCodeRepository qrCodeRepository;
    private final QrCodeVisitorRepository qrCodeVisitorRepository;
    private final ApplicationContext context;
    private static final Random random = new Random();

    public QRCodeTgBot(
            TelegramBotConfig telegramBotConfig,
            UserService userService,
            TgBotUtils tgBotUtils,
            UserRepository userRepository,
            QrCodeRepository qrCodeRepository,
            QrCodeVisitorRepository qrCodeVisitorRepository,
            ApplicationContext context
    ) {
        this.telegramBotConfig = telegramBotConfig;
        this.userService = userService;
        this.tgBotUtils = tgBotUtils;
        this.userRepository = userRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.qrCodeVisitorRepository = qrCodeVisitorRepository;
        this.context = context;
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
        QrCodeService qrCodeService = getQrCodeService();
        if (isCommand(update)) {
            String text = update.getMessage().getText();
            processCommands(user, text);
            return;
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            processCallbackQuery(user, callbackQuery);
        }
        if (user.getStepOfGenerationCode() == 20 && update.hasMessage()) {
            setCustomColorForForeGround(user, update);
        } else if (user.getStepOfGenerationCode() == 30 && update.hasMessage()) {
            setCustomColorForBackGround(user, update);
        }

        if (user.isOnFinalStepOfCreation() && update.hasMessage()) {
            qrCodeService.generateQrCode(user, update);
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
    public void sendPhoto(Long chatId, InputFile qrFile) {
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
            case "/start" -> {
                sendMessageToUser(user, "\uD83D\uDC4B *Welcome to QrCodeGenBot!*");
                sendMessageToUser(user,
                        """
                                We're thrilled to have you here! \uD83D\uDE0A Let's get started together.

                                ✨ *What Can You Do Here?*
                                """
                );
                sendMessageToUser(user,
                        """
                                *Create Custom QR Codes* \uD83C\uDFA8
                                Design unique QR codes in any color you can imagine. Only your creativity sets the limit!
                                """
                );
                sendMessageToUser(user,
                        """
                                *Track Your Codes* \uD83D\uDCCA
                                Monitor how many times your codes are scanned.
                                See from which country the scans are coming from.
                                We might even give you the IP addresses of those who scanned them (shh, it's a secret! \uD83E\uDD2B).
                                """
                );
                sendMessageToUser(user,
                        """
                                *Dynamic Links* \uD83D\uDD04
                                Change the link (or text) that opens when people scan your QR code, without altering the QR code image itself!
                                """
                );
                sendMessageToUser(user,
                        "\uD83D\uDCA1 *A Little Hint:*\n" +
                                "All these amazing features come at a price, but don't worry\\—it's not much! Just a small investment for unlimited creativity and insights.\uD83D\uDCB8"
                );
                sendMessageToUser(user,
                        "\uD83C\uDD93 *Free Version Available:*\n" +
                                "You can also use our free version to generate QR codes that expire in 2 weeks. A perfect way to try out our service! \uD83D\uDE80"
                );
            }
            case "/generateqrcode" -> processQRCodeGenerating(user);
            case "/profile" -> showUserProfile(user);
            case "/showmyqrcodes" -> showUserQrCodes(user);
            case "/info" -> {
                sendMessageToUser(user, "ℹ️ *QR Code Types and Features*");
                sendMessageToUser(user,
                        """
                                *Basic QR Code* \uD83D\uDD11
                                - Works for 2 weeks, after which it expires. ⏳
                                - Only default or random colors are available for customization. 🎨
                                - You can have a maximum of 5 basic QR codes. ✋
                                - See the number of times the link is clicked from the statistics. 📈
                                - You can't actively change the link. 🚫🔗
                                """
                );
                sendMessageToUser(user,
                        """
                                *Permanent VIP* \uD83D\uDD25👑
                                - Works forever. ♾️
                                - Any customization is available. 🌈
                                - Access to all statistics. 📊
                                - Full access to all features we offer. 🎁
                                """
                );
                sendMessageToUser(user,
                        """
                                *Raw Permanent* \uD83E\uDD16⚡
                                - Works indefinitely, even after the extinction of humanity! \uD83D\uDE0E💀
                                - Does not require the Internet to reveal the content behind the QR code. 🌐❌
                                - Most common QR code with open customization. 🛠️
                                - You can't actively change the link or track statistics. 🚫📊
                                """
                );
            }
        }
    }


    private void showUserProfile(TgUser user) {
        String profileMessage = "\uD83D\uDC64 *Your Profile*\n" +
                "\n" +
                "👤 *Username:* " + (user.getTgUsername() != null ? "@" + user.getTgUsername() : "Unknown Adventurer \uD83E\uDDD0") + "\n" +
                "🎭 *Role:* " + (user.getRole() != null ? user.getRole().name() : "Mystery Role \uD83D\uDC40") + "\n" +
                "🆔 *Telegram User ID:* " + (user.getTelegramUserId() != null ? user.getTelegramUserId() : "Not Available \uD83D\uDE36") + "\n" +
                "\n";

        sendMessageToUser(user, profileMessage);
    }

    private void showUserQrCodes(TgUser user) {
        List<QrCode> qrCodes = getQrCodeService().getQrCodesByUser(user);

        if (qrCodes == null || qrCodes.isEmpty()) {
            sendMessageToUser(user, "🚫 *You have no QR codes yet!* \n" +
                    "It looks like you haven't created any QR codes. What are you waiting for? Let’s get those creative juices flowing and make some magic! 🎨✨");
            return;
        }


        sendMessageToUser(user, "📋 *Your QR Codes*");

        for (QrCode qrCode : qrCodes) {
            if (!qrCode.getIsCreated()) {
                continue;
            }
            String qrCodeMessage = "🔹 *QR Code ID:* `" + qrCode.getUuid() + "`\n" +
                    "\uD83C\uDFA9 *Type:* " + qrCode.getType() + "\n" +
                    "\uD83D\uDC85 *Text:* " + qrCode.getText() + "\n" +
                    "🔗 *Link:* [" + qrCode.getText() + "](" + qrCode.getFullLink() + ")\n" +
                    "📅 *Created On:* " + (qrCode.getCreationDate() != null ? qrCode.getCreationDate().toLocalDate().toString() : "Unknown Date") + "\n" +
                    "⏳ *Expiration Time:* " + (qrCode.getExpirationTime() != null ? qrCode.getExpirationTime().toLocalDate().toString() : "Never") + "\n" +
                    "🔄 *Active:* " + (qrCode.getIsActive() != null && qrCode.getIsActive() ? "Yes" : "No") + "\n" +
                    "🔍 *Scan Count:* " + (qrCode.getQrCodeScanCount() != null ? qrCode.getQrCodeScanCount() : 0) + "\n";

            List<QrCodeVisitor> qrCodeVisitors = qrCodeVisitorRepository.findAllByVisitedQrCode(qrCode);
            if (!qrCodeVisitors.isEmpty() && !qrCode.getType().equals("basic")) {
                qrCodeMessage += "🔍 *Unique Scans:* " + qrCodeVisitors.size() + "\n";
            }
            sendMessageToUser(user, qrCodeMessage);
        }

        sendMessageToUser(user, "aboba");
    }


    private void processCallbackQuery(TgUser user, CallbackQuery callbackQuery) {
        QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(null);
        switch (callbackQuery.getData()) {
            case "Basic qr code":
                qrCode.setExpirationTime(LocalDateTime.now().plusWeeks(2));
                qrCode.setType("basic");
                qrCodeRepository.save(qrCode);
                showUserCustomizationForBasicQRCode(user);
                break;
            case "Permanent VIP":
                qrCode.setExpirationTime(LocalDateTime.now().plusYears(1000));
                qrCode.setType("vip");
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForForeground(user);
                break;
            case "Permanent RAW":
                qrCode.setType("raw");
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForForeground(user);
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
            case "⬛️Black foreGround ":
                qrCode.setForegroundColor(Color.BLACK);
                qrCodeRepository.save(qrCode);
                showUserPossibleCustomizationForBackGround(user);
                break;
            case "⬜️White foreGround ":
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
            case "⬛️Black BackGround ":
                qrCode.setBackgroundColor(Color.BLACK);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "⬜️White BackGround ":
                qrCode.setBackgroundColor(Color.WHITE);
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "Choose Random color for BackGround":
                qrCode.setBackgroundColor(getRandomColor());
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83C\uDF08 Choose Random colors":
                qrCode.setBackgroundColor(getRandomColor());
                qrCode.setForegroundColor(getRandomColor());
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "⬅️ back":
                processBackButton(user);
                break;
            default:
                // Handle unknown callback data
                break;
        }
    }

    @SneakyThrows
    private void showUserCustomizationForBasicQRCode(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            editMessage.setParseMode(ParseMode.MARKDOWNV2);
            editMessage.setText("Choose colors for QR code: ");
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(
                    List.of(
                            "\uD83D\uDD33" + "Choose default",
                            "\uD83C\uDF08 Choose Random colors",
                            "⬅️ back"

                    ));
            editMessage.setReplyMarkup(markup);
            execute(editMessage);
        }
        user.setStepOfGenerationCode(2);
        userRepository.save(user);
    }

    private void processBackButton(TgUser user) {
        QrCode notCreatedQrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user)
                .orElseThrow(
                        () -> new RuntimeException("Qr code by provided user wasn't found")
                );
        if(notCreatedQrCode.getType().equals("basic")){
            if(user.getStepOfGenerationCode()==4){
                showUserCustomizationForBasicQRCode(user);
                return;
            }
        }
        switch (user.getStepOfGenerationCode()) {
            case 2 -> showOptionsToChooseType(user);
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
                            "⬛️" + "Black foreGround ",
                            "⬜️" + "White foreGround ",
                            "\uD83D\uDD33" + "Choose default",
                            "Choose Random color for foreGround",
                            "Create your own color for foreGround",
                            "⬅️ back"
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
            editMessage.setText("Write your color in Hexadecimal format(e.g., \"#RRGGBB\" or \"RRGGBB\"): \n Example: #FF5733");
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
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
                            "⬛️" + "Black BackGround ",
                            "⬜️" + "White BackGround ",
                            "Choose Random color for BackGround",
                            "Create your own color for BackGround",
                            "⬅️ back"
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
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
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
        getQrCodeService().generateQrCode(user);
        showOptionsToChooseType(user);
    }


    @SneakyThrows
    public void sendMessageToUser(TgUser user, String text) {
        execute(SendMessage.builder()
                .chatId(user.getChatId().toString())
                .text(text)
                .parseMode(ParseMode.MARKDOWN)
                .build()
        );
    }

    @SneakyThrows
    public void generateMenuButtons() {
        List<BotCommand> listOfCommands = List.of(
                new BotCommand("/start", "start to work with bot."),
                new BotCommand("/profile", "open your profile"),
                new BotCommand("/info", "information that you need to know about this bot"),
                new BotCommand("/showmyqrcodes", "show all qr codes that you have"),
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
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return new Color(red, green, blue);
    }

    @SneakyThrows
    private void showOptionsToChooseType(TgUser user) {
        InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("Basic qr code", "Permanent VIP", "Permanent RAW"));

        if (user.getMessageId() != null) {
            EditMessageText messageText = new EditMessageText();
            messageText.setChatId(user.getChatId());
            messageText.setMessageId(user.getMessageId());
            messageText.setText("Choose a type of a QR code");
            messageText.setReplyMarkup(markup);
                execute(messageText);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(user.getChatId());
            message.setText("Choose a type of a QR code");
            message.setReplyMarkup(markup);
                Integer messageId = execute(message).getMessageId();
                user.setMessageId(messageId);

        }

        user.setGenerateQrCodeRightNow(true);
        userRepository.save(user);
    }

    public QrCodeService getQrCodeService(){
        return context.getBean(QrCodeService.class);
    }

}


