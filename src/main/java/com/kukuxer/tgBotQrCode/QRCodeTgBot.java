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


import java.time.LocalDateTime;

import java.util.List;


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

        if (user.isOnFinalStepOfCreation() && update.hasMessage()) {
            generateQrCode(user, update);
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
        switch (callbackQuery.getData()) {
            case "Basic qr code":
                QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow();
                qrCode.setExpirationTime(LocalDateTime.now().plusWeeks(2));
                qrCodeRepository.save(qrCode);
                tellUserToWriteTextForQRCode(user);
                break;
            case "Permanent VIP":


                break;
            case "Permanent RAW":


                break;
            default:
                // Handle unknown callback data
                break;
        }
    }

    @SneakyThrows
    private void tellUserToWriteTextForQRCode(TgUser user) {
        Integer attackerMessageId = user.getMessageId();
        if (attackerMessageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(attackerMessageId);
            editMessage.setParseMode(ParseMode.MARKDOWNV2);
            editMessage.setText("Send link or text for your qr code:");
            execute(editMessage);
        }
        user.setOnFinalStepOfCreation(true);
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

