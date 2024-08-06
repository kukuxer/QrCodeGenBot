package com.kukuxer.tgBotQrCode.tgBot;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeRepository;
import com.kukuxer.tgBotQrCode.user.TgUser;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class TgBotUtils {
    private static final Random random = new Random();
    private final QrCodeRepository qrCodeRepository;
    private final ApplicationContext context;

    public TgBotUtils(QrCodeRepository qrCodeRepository, ApplicationContext context) {
        this.qrCodeRepository = qrCodeRepository;
        this.context = context;
    }

    public MessagesForUser getMessages() {
        return context.getBean(MessagesForUser.class);
    }

    public InlineKeyboardMarkup createMarkup(List<String> buttonsName) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String name : buttonsName) {
            List<InlineKeyboardButton> row = createButtonRow(name);
            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }

    private List<InlineKeyboardButton> createButtonRow(String name) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = createButton(name);
        row.add(button);
        return row;
    }

    private InlineKeyboardButton createButton(String name) {

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(name);
        button.setCallbackData(name);
        return button;
    }

    public boolean isCommand(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            return text.startsWith("/");
        }
        return false;
    }

    public static Color getRandomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        return new Color(red, green, blue);
    }
}
