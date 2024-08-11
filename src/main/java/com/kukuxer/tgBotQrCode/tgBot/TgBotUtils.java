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

    public InlineKeyboardMarkup createMarkupForQrCode(TgUser user) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<QrCode> qrCodes = qrCodeRepository.findAllByCreator(user);
        for (QrCode qrCode : qrCodes) {
            if (qrCode.getIsCreated()) {
                List<InlineKeyboardButton> row = createButtonRowForQrCode(qrCode);
                rows.add(row);
            }
        }
        markup.setKeyboard(rows);
        return markup;
    }

    public InlineKeyboardMarkup createMarkupForManageQrCode(QrCode qrCode) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        List<InlineKeyboardButton> row = getManagingButtons(qrCode);
        rows.add(row);


        List<InlineKeyboardButton> backRow = getBackButtonRow();
        rows.add(backRow);
        markup.setKeyboard(rows);
        return markup;
    }

    private List<InlineKeyboardButton> getBackButtonRow() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("⬅️ back");
        button.setCallbackData("⬅️ back");
        List<InlineKeyboardButton> list = new ArrayList<>();
        list.add(button);
        return list;
    }

    private List<InlineKeyboardButton> getManagingButtons(QrCode qrCode) {
        InlineKeyboardButton changeLink = new InlineKeyboardButton();
        changeLink.setText("\uD83D\uDD04 change link");
        changeLink.setCallbackData("change " + qrCode.getUuid());

        InlineKeyboardButton view = new InlineKeyboardButton();
        view.setText("👁️Check visitors");
        view.setCallbackData("view " + qrCode.getUuid());

        InlineKeyboardButton delete = new InlineKeyboardButton();
        delete.setText("🗑️Delete");
        delete.setCallbackData("delete " + qrCode.getUuid());


        List<InlineKeyboardButton> list = new ArrayList<>();
        list.add(changeLink);
        list.add(view);
        list.add(delete);
        return list;
    }

    private List<InlineKeyboardButton> createButtonRowForQrCode(QrCode qrCode) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = createButtonForQrCode(qrCode);
        row.add(button);

        return row;
    }

    private InlineKeyboardButton createButtonForQrCode(QrCode qrCode) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(qrCode.getText());
        button.setCallbackData("qr " + qrCode.getUuid());
        return button;
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
        List<String> listOfCommands = List.of(
                "/generateqrcode",
                "/start",
                "/showmyqrcodes",
                "/profile",
                "/info",
                "/buyvip",
                "/showMySecretCode"
        );
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            return listOfCommands.contains(text);
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
