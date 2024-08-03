package com.kukuxer.tgBotQrCode;

import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.awt.*;

@Component
@RequiredArgsConstructor
public class QRCodeTgBot extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;
    private final QRCodeGenerator qrCodeGenerator;
    private final UserService userService;

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


        InputFile qrFile = qrCodeGenerator.getQRCodeImageFile(messageText, Color.RED, Color.BLACK);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());
        sendPhoto.setPhoto(qrFile);
        execute(sendPhoto);

    }


    private boolean isCommand(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (text.startsWith("/")) {
                return true;
            }
        }
        return false;
    }

    private void processCommands(TgUser user, String text) {
        switch (text) {
            case "/start" -> sendMessageToUser(
                    user,
                    "Hello, welcome to the bot, that will help you to create your qr codes!"
            );

        }

    }

    @SneakyThrows
    public void sendMessageToUser(TgUser user, String text) {
        execute(SendMessage.builder()
                .chatId(user.getChatId().toString())
                .text(text)
                .build()
        );
    }


}

