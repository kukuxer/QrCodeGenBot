package com.kukuxer.tgBotQrCode;

import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.awt.*;
import java.util.List;

@Component
//@RequiredArgsConstructor
public class QRCodeTgBot extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;
    private final QRCodeGenerator qrCodeGenerator;
    private final UserService userService;

    public QRCodeTgBot(
            TelegramBotConfig telegramBotConfig,
            QRCodeGenerator qrCodeGenerator,
            UserService userService
            ){
        this.telegramBotConfig = telegramBotConfig;
        this.qrCodeGenerator = qrCodeGenerator;
        this.userService = userService;
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
        }

        if() {
            InputFile qrFile = qrCodeGenerator.getQRCodeImageFile(text, Color.RED, Color.BLACK);
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId.toString());
            sendPhoto.setPhoto(qrFile);
            execute(sendPhoto);
        }
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
                    "Hello, welcome to the bot, that will help you to create your qr codes!"
            );
            case "/generateqrcode" -> processQRCodeGenerating(user, text);
        }
    }

    private void processQRCodeGenerating(TgUser user, String text) {
        sendMessageToUser(user,"Choose QR code type: ");

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
    public void generateMenuButtons(){
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


}

