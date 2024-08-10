package com.kukuxer.tgBotQrCode.tgBot;

import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitor;
import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitorRepository;
import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeService;
import com.kukuxer.tgBotQrCode.user.Role;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessagesForUser {
    private final QRCodeTgBot tgBot;
    private final TgBotUtils tgBotUtils;
    private final UserRepository userRepository;
    private final QrCodeVisitorRepository qrCodeVisitorRepository;
    private final ApplicationContext context;

    public MessagesForUser(QRCodeTgBot tgBot, TgBotUtils tgBotUtils, UserRepository userRepository, QrCodeVisitorRepository qrCodeVisitorRepository, ApplicationContext context) {

        this.tgBot = tgBot;
        this.tgBotUtils = tgBotUtils;
        this.userRepository = userRepository;
        this.qrCodeVisitorRepository = qrCodeVisitorRepository;
        this.context = context;
    }

    @SneakyThrows
    void showUserPossibleCustomizationForForeground(TgUser user) {
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
            tgBot.execute(editMessage);
        }
        user.setStepOfGenerationCode(2);
        userRepository.save(user);
    }

    @SneakyThrows
    void askUserToWriteCustomColorInHexadecimalFormat(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            editMessage.setText("Write your color in Hexadecimal format(e.g., \"#RRGGBB\" or \"RRGGBB\"): \n Example: #FF5733");
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
            editMessage.setReplyMarkup(markup);
            tgBot.execute(editMessage);
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
    public void showUserPossibleCustomizationForBackGround(TgUser user) {
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
            tgBot.execute(editMessage);
        }
        user.setStepOfGenerationCode(3);
        userRepository.save(user);
    }

    @SneakyThrows
    void showUserCustomizationForBasicQRCode(TgUser user) {
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
            tgBot.execute(editMessage);
        }
        user.setStepOfGenerationCode(2);
        userRepository.save(user);
    }

    @SneakyThrows
    public void showOptionsToChooseType(TgUser user) {
        InlineKeyboardMarkup markup;
        if (user.getRole().equals(Role.VIP)) {
            markup = tgBotUtils.createMarkup(List.of("\uD83D\uDCC5 Basic qr code", "\uD83D\uDC51 Permanent VIP", "\uD83D\uDEE0️ Permanent RAW"));
        } else {
            markup = tgBotUtils.createMarkup(List.of("\uD83D\uDCC5 Basic qr code", "\uD83D\uDEAB Unlock VIP for other type's \uD83D\uDEAB"));
        }


        if (user.getMessageId() != null) {
            EditMessageText messageText = new EditMessageText();
            messageText.setChatId(user.getChatId());
            messageText.setMessageId(user.getMessageId());
            messageText.setText("Choose a type of a QR code");
            messageText.setReplyMarkup(markup);
            tgBot.execute(messageText);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(user.getChatId());
            message.setText("Choose a type of a QR code");
            message.setReplyMarkup(markup);
            Integer messageId = tgBot.execute(message).getMessageId();
            user.setMessageId(messageId);

        }
        user.setGenerateQrCodeRightNow(true);
        userRepository.save(user);
    }

    void sendMessagesAfterStartCommand(TgUser user) {
        tgBot.sendMessageToUser(user, "\uD83D\uDC4B *Welcome to QrCodeGenBot!*");
        tgBot.sendMessageToUser(user,
                """
                                            
                        ✨ *What Can You Do Here?*
                                                
                        *Create Custom QR Codes* \uD83C\uDFA8
                        Design unique QR codes in any color you can imagine.
                        """
        );
        tgBot.sendMessageToUser(user,
                """
                        *Track Your Codes* \uD83D\uDCCA
                        Monitor how many times your codes are scanned.
                        See from which country the scans are coming from.
                        We might even give you the IP addresses of those who scanned them (shh, it's a secret! \uD83E\uDD2B).
                        """
        );
        tgBot.sendMessageToUser(user,
                """
                        *Dynamic Links* \uD83D\uDD04
                        Change the link (or text) that opens when people scan your QR code, without altering the QR code image itself!
                        """
        );
        tgBot.sendMessageToUser(user,
                "\uD83C\uDD93 *Free Version Available:*\n" +
                        "You can also use our free version to generate QR codes that expire in 2 weeks. A perfect way to try out our service! \uD83D\uDE80");
    }

    void sendMessageAfterInfoCommand(TgUser user) {
        tgBot.sendMessageToUser(user, "ℹ️ *QR Code Types and Features*");
        tgBot.sendMessageToUser(user,
                """
                        *Basic QR Code* \uD83D\uDD11
                        - Works for 2 weeks, after which it expires. ⏳
                        - Only default or random colors are available for customization. 🎨
                        - You can have a maximum of 5 basic QR codes. ✋
                        - See the number of times the link is clicked from the statistics. 📈
                        - You can't actively change the link. 🚫🔗
                        """
        );
        tgBot.sendMessageToUser(user,
                """
                        *Permanent VIP* \uD83D\uDD25👑
                        - Works forever. ♾️
                        - Any customization is available. 🌈
                        - Access to all statistics. 📊
                        - Full access to all features we offer. 🎁
                        """
        );
        tgBot.sendMessageToUser(user,
                """
                        *Raw Permanent* \uD83E\uDD16⚡
                        - Works indefinitely, even after the extinction of humanity! \uD83D\uDE0E💀
                        - Does not require the Internet to reveal the content behind the QR code. 🌐❌
                        - Most common QR code with open customization. 🛠️
                        - You can't actively change the link or track statistics. 🚫📊
                        """);
    }

    void showUserProfile(TgUser user) {
        String profileMessage = "\uD83D\uDC64 *Your Profile*\n" +
                "\n" +
                "👤 *Username:* " + (user.getTgUsername() != null ? "@" + user.getTgUsername() : "Unknown Adventurer \uD83E\uDDD0") + "\n" +
                "🎭 *Role:* " + (user.getRole() != null ? user.getRole().name() : "Mystery Role \uD83D\uDC40") + "\n" +
                "🆔 *Telegram User ID:* " + (user.getTelegramUserId() != null ? "`" + user.getChatId() + "`" : "Not Available \uD83D\uDE36") + "\n" +
                "\n";

        tgBot.sendMessageToUser(user, profileMessage);
    }


    public void showUserQrCodeVisitors(TgUser user, List<QrCodeVisitor> qrCodeVisitors) {

        if (qrCodeVisitors == null || qrCodeVisitors.isEmpty()) {
            tgBot.sendMessageToUser(user, "🚫 *You have no Visitors or this QR code * \n");
            return;
        }
        tgBot.sendMessageToUser(user, "📋 *Your QR code visitors*");

        for (QrCodeVisitor qrCodeVisitor : qrCodeVisitors) {

            String qrCodeVisitorMessage =
                    "🌍 *IP Address:* " + qrCodeVisitor.getIp() + "\n" +
                            "🌎 *Country:* " + qrCodeVisitor.getCountry() + "\n" +
                            "🌆 *City:* " + qrCodeVisitor.getCity() + "\n" +
                            "📅 *Visited On:* " + (qrCodeVisitor.getVisitedTime() != null ? qrCodeVisitor.getVisitedTime().toLocalDate().toString() : "Unknown Date") + "\n";
            tgBot.sendMessageToUser(user, qrCodeVisitorMessage);
        }
    }

    public void showUserQrCodeVisitorsNoVIP(TgUser user, List<QrCodeVisitor> qrCodeVisitors) {
        if (qrCodeVisitors == null || qrCodeVisitors.isEmpty()) {
            tgBot.sendMessageToUser(user, "🚫 *You have no Visitors or this QR code * \n");
            return;
        }
        tgBot.sendMessageToUser(user, "📋 *Your QR code visitors*");

        for (QrCodeVisitor qrCodeVisitor : qrCodeVisitors) {

            String qrCodeVisitorMessage =
                    "🌍 *IP Address:* " + " unlock with VIP" + "\n" +
                            "🌎 *Country:* " + qrCodeVisitor.getCountry() + "\n" +
                            "🌆 *City:* " + " unlock with VIP" + "\n" +
                            "📅 *Visited On:* " + (qrCodeVisitor.getVisitedTime() != null ? qrCodeVisitor.getVisitedTime().toLocalDate().toString() : "Unknown Date") + "\n";
            tgBot.sendMessageToUser(user, qrCodeVisitorMessage);
        }
    }

    @SneakyThrows
    public void sendMessageForManagingQrCodes(TgUser user) {
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId());
        message.setText("If you want to change the link, delete, view, or get information about who visited your QR code, you can use the following buttons:\n"
        );
        InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("\uD83D\uDD04 change link", "🗑️Delete", "👁️Check visitors"));
        message.setReplyMarkup(markup);
        Integer messageId = tgBot.execute(message).getMessageId();
        user.setAdditionalMessageId(messageId);
        userRepository.save(user);
    }

    @SneakyThrows
    public void sendMessageForDeletingQRCode(TgUser user) {
        Integer messageId = user.getAdditionalMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
            editMessage.setReplyMarkup(markup);
            editMessage.setParseMode(ParseMode.MARKDOWN);
            editMessage.setText("🗑️ Please send the *ID* of the QR code you want to delete. But be careful, once you delete it, it cannot be restored! 😱\n" +
                    "You can find the QR code ID in your QR code list and copy the ID just by clicking it! 📋🔗");
            tgBot.execute(editMessage);
        }
    }

    @SneakyThrows
    public void sendMessageForChangingQRCodeLink(TgUser user) {
        Integer messageId = user.getAdditionalMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            editMessage.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardMarkup markupForQrCode = tgBotUtils.createMarkupForQrCode(user);
            editMessage.setReplyMarkup(markupForQrCode);
            editMessage.setText("Choose QR code which you want to change link");
            tgBot.execute(editMessage);
        }
    }

    @SneakyThrows
    public void showQrCodes(TgUser user) {
        List<QrCode> qrCodes = getQrCodeService().getQrCodesByUser(user);

        if (qrCodes == null || qrCodes.isEmpty()) {
            tgBot.sendMessageToUser(user, "🚫 *You have no QR codes yet!* \n" +
                    "It looks like you haven't created any QR codes. What are you waiting for? Let’s get those creative juices flowing and make some magic! 🎨✨");
            return;
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        InlineKeyboardMarkup markupForQrCode = tgBotUtils.createMarkupForQrCode(user);
        sendMessage.setReplyMarkup(markupForQrCode);
        sendMessage.setText("Choose QR code which you want to manage");
        Message execute = tgBot.execute(sendMessage);

        user.setStepOfManagingCodes(1);
        user.setAdditionalMessageId(execute.getMessageId());
        userRepository.save(user);
    }

    @SneakyThrows
    public void showQrCode(TgUser user, QrCode qrCode) {
        String qrCodeMessage =
                "\uD83C\uDFA9 *Type:* " + qrCode.getType() + "\n" +
                        "🔗 *Link:* [" + qrCode.getText() + "](" + qrCode.getFullLink() + ")\n" +
                        "📅 *Created On:* " + (qrCode.getCreationDate() != null ? qrCode.getCreationDate().toLocalDate().toString() : "Unknown Date") + "\n" +
                        "⏳ *Expiration Time:* " + (qrCode.getExpirationTime() != null ? qrCode.getExpirationTime().toLocalDate().toString() : "Never") + "\n" +
                        "🔄 *Active:* " + (qrCode.getIsActive() != null && qrCode.getIsActive() ? "Yes" : "No") + "\n" +
                        "🔍 *Scan Count:* " + (qrCode.getQrCodeScanCount() != null ? qrCode.getQrCodeScanCount() : 0) + "\n";
        List<QrCodeVisitor> qrCodeVisitors = qrCodeVisitorRepository.findAllByVisitedQrCode(qrCode);
        if (!qrCodeVisitors.isEmpty() && !qrCode.getType().equals("basic")) {
            qrCodeMessage += "🔍 *Unique Scans:* " + qrCodeVisitors.size() + "\n";
        }


        EditMessageText message = new EditMessageText();
        message.setChatId(user.getChatId());
        message.setText(qrCodeMessage);
        InlineKeyboardMarkup markup = tgBotUtils.createMarkupForManageQrCode(qrCode);
        message.setReplyMarkup(markup);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setMessageId(user.getAdditionalMessageId());
        tgBot.execute(message);

    }

    @SneakyThrows
    public void askUserToWriteTextForChangingLink(TgUser user, QrCode qrCode) {
        EditMessageText message = new EditMessageText();
        message.setChatId(user.getChatId());
        message.setText("Current text: " + qrCode.getText() + " Please send a new link to replace it");
        InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
        message.setReplyMarkup(markup);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setMessageId(user.getAdditionalMessageId());
        tgBot.execute(message);
        user.setWantToChangeLink(true);
        user.setQrCodeIdToChange(qrCode.getUuid());
        user.setStepOfManagingCodes(2);
        userRepository.save(user);
    }

    @SneakyThrows
    public void sendMessageForCheckingQrCodeVisitors(TgUser user) {
        Integer messageId = user.getAdditionalMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
            editMessage.setReplyMarkup(markup);
            editMessage.setParseMode(ParseMode.MARKDOWN);
            editMessage.setText("👁️ Please send the *ID* of the QR code you want to Check visitors. \n" +
                    "You can find the QR code ID in your QR code list and copy the ID just by clicking it! 📋🔗");
            tgBot.execute(editMessage);
        }
    }


    @SneakyThrows
    public void tellUserToWriteTextForQRCode(TgUser user) {
        Integer messageId = user.getMessageId();
        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(user.getChatId());
            editMessage.setMessageId(messageId);
            InlineKeyboardMarkup markup = tgBotUtils.createMarkup(List.of("⬅️ back"));
            editMessage.setReplyMarkup(markup);
            editMessage.setText("Send link or text for your qr code:");
            tgBot.execute(editMessage);
        }
        user.setOnFinalStepOfCreation(true);
        user.setStepOfGenerationCode(4);
        userRepository.save(user);
    }

    public QrCodeService getQrCodeService() {
        return context.getBean(QrCodeService.class);
    }

    public void sendMessageToBuyVIP(TgUser user) {
        tgBot.sendMessageToUser(user, "\uD83D\uDCB8 *Upgrade to VIP!*");

        tgBot.sendMessageToUser(user,
                """
                        \uD83D\uDCB0 *Why Go VIP?*
                                        
                        - \uD83D\uDD25 **Unlimited QR Codes**: Create as many QR codes as you need without any limits.
                        - \uD83C\uDF10 **Permanent Links**: Your QR codes never expire. Perfect for long-term projects or businesses.
                        - \uD83D\uDCCA **Advanced Analytics**: Get deeper insights into who scans your codes, including detailed IP tracking, city, and more.
                        - \uD83D\uDD35 **Priority Support**: Enjoy faster responses and priority handling for any issues you may encounter.
                        """
        );

        tgBot.sendMessageToUser(user,
                "\uD83D\uDCB3 *How to Buy VIP:*\n" +
                        "To upgrade to VIP, with *Monobank*: simply send *5 гривен* with the comment of your * Telegram user ID*, which you can find in your profile.\n\n" +
                        "Your ID: `" + user.getChatId() + "`\n\n" +
                        "\uD83D\uDC41 *Note:* After sending the payment, your account will be upgraded automatically within a few minutes.\n" +
                        "With *any other bank*: simply write to support 'I want to get VIP' and you will receive a response within one business day.\n" +
                        "If you have any issues, don't hesitate to reach out to our support team! \uD83D\uDCDE"
        );
    }

}
