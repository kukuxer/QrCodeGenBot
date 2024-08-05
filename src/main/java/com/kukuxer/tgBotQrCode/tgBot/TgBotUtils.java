package com.kukuxer.tgBotQrCode.tgBot;

import com.kukuxer.tgBotQrCode.user.TgUser;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgBotUtils {


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
}
