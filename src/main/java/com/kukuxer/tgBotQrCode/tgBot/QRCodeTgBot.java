package com.kukuxer.tgBotQrCode.tgBot;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeRepository;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeService;
import com.kukuxer.tgBotQrCode.user.Role;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.awt.*;
import java.time.LocalDateTime;

import java.util.List;

import static java.util.Objects.isNull;


@Component
@Slf4j
public class QRCodeTgBot extends TelegramLongPollingBot {
    private final TelegramBotConfig telegramBotConfig;
    private final UserService userService;
    private final TgBotUtils tgBotUtils;
    private final UserRepository userRepository;
    private final QrCodeRepository qrCodeRepository;
    private final ApplicationContext context;


    public QRCodeTgBot(
            TelegramBotConfig telegramBotConfig,
            UserService userService,
            TgBotUtils tgBotUtils,
            UserRepository userRepository,
            QrCodeRepository qrCodeRepository,
            ApplicationContext context
    ) {
        this.telegramBotConfig = telegramBotConfig;
        this.userService = userService;
        this.tgBotUtils = tgBotUtils;
        this.userRepository = userRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.context = context;
        generateMenuButtons();
    }

    public QrCodeService getQrCodeService() {
        return context.getBean(QrCodeService.class);
    }

    public MessagesForUser getMessages() {
        return context.getBean(MessagesForUser.class);
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


        if (tgBotUtils.isCommand(update)) {
            processCommands(user, update.getMessage().getText());
            return;
        }

        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            if (user.isWantToDelete()) {
                qrCodeService.deleteQrCode(user, messageText);
                return;
            }
            if (user.isWantToCheckVisitors()) {
                qrCodeService.showQRCodeVisitorsById(user, messageText);
                return;
            }
            if (!isNull(user.getQrCodeIdToChange()) && user.isWantToChangeLink()) {
                qrCodeService.changeQrCodeLink(user, messageText);
                return;
            }
            if (user.getStepOfGenerationCode() == 20) {
                qrCodeService.setCustomColorForForeGround(user, update);
                return;
            }
            if (user.getStepOfGenerationCode() == 30) {
                qrCodeService.setCustomColorForBackGround(user, update);
                return;
            }
            if (user.isOnFinalStepOfCreation()) {
                qrCodeService.generateQrCode(user, update);
                return;
            }
        }

        if (update.hasCallbackQuery()) {
            if (user.isWantToChangeLink()) {
                qrCodeService.processChangeQrCodeLink(user, update.getCallbackQuery().getData());
            }
            processCallbackQuery(user, update.getCallbackQuery());


        }
    }

    private void processCommands(TgUser user, String text) {
        switch (text) {
            case "/start" -> getMessages().sendMessagesAfterStartCommand(user);
            case "/generateqrcode" -> getQrCodeService().processQRCodeGenerating(user);
            case "/profile" -> getMessages().showUserProfile(user);
            case "/showmyqrcodes" -> getMessages().showUserQrCodes(user);
            case "/info" -> getMessages().sendMessageAfterInfoCommand(user);
            case "/buyvip" -> getMessages().sendMessageToBuyVIP(user);
            case "/showMySecretCode" -> sendMessageToUser(user, String.valueOf(user.getSecretCode()));
        }
    }

    private void processCallbackQuery(TgUser user, CallbackQuery callbackQuery) {
        QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElse(null);
        switch (callbackQuery.getData()) {
            case "\uD83D\uDCC5 Basic qr code":
                qrCode.setExpirationTime(LocalDateTime.now().plusWeeks(2));
                qrCode.setType("basic");
                qrCodeRepository.save(qrCode);
                getMessages().showUserCustomizationForBasicQRCode(user);
                break;
            case "\uD83D\uDC51 Permanent VIP":
                if (user.getRole().equals(Role.VIP)) {
                    qrCode.setExpirationTime(LocalDateTime.now().plusYears(1000));
                    qrCode.setType("vip");
                    qrCodeRepository.save(qrCode);
                    getMessages().showUserPossibleCustomizationForForeground(user);
                } else {
                    sendMessageToUser(user, "you need VIP status for this");
                }
                break;
            case "\uD83D\uDEE0️ Permanent RAW":
                if (user.getRole().equals(Role.VIP)) {
                    qrCode.setType("raw");
                    qrCodeRepository.save(qrCode);
                    getMessages().showUserPossibleCustomizationForForeground(user);

                } else {
                    sendMessageToUser(user, "you need VIP status for this");
                }
                break;
            case "\uD83D\uDFE5Red foreGround ":
                qrCode.setForegroundColor(Color.RED);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE7Orange foreGround ":
                qrCode.setForegroundColor(Color.ORANGE);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE8Yellow foreGround ":
                qrCode.setForegroundColor(Color.YELLOW);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE9Green foreGround ":
                qrCode.setForegroundColor(Color.GREEN);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFE6Blue foreGround ":
                qrCode.setForegroundColor(Color.BLUE);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFEAPurple foreGround ":
                qrCode.setForegroundColor(Color.magenta);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDFEBBrown foreGround ":
                qrCode.setForegroundColor(new Color(165, 42, 42));
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "⬛️Black foreGround ":
                qrCode.setForegroundColor(Color.BLACK);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "⬜️White foreGround ":
                qrCode.setForegroundColor(Color.WHITE);
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "\uD83D\uDD33Choose default":
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "Choose Random color for foreGround":
                qrCode.setForegroundColor(TgBotUtils.getRandomColor());
                qrCodeRepository.save(qrCode);
                getMessages().showUserPossibleCustomizationForBackGround(user);
                break;
            case "Create your own color for foreGround", "Create your own color for BackGround":
                getMessages().askUserToWriteCustomColorInHexadecimalFormat(user);
                break;
            case "\uD83D\uDFE5Red BackGround ":
                qrCode.setBackgroundColor(Color.RED);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE7Orange BackGround ":
                qrCode.setBackgroundColor(Color.ORANGE);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE8Yellow BackGround ":
                qrCode.setBackgroundColor(Color.YELLOW);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE9Green BackGround ":
                qrCode.setBackgroundColor(Color.GREEN);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFE6Blue BackGround ":
                qrCode.setBackgroundColor(Color.BLUE);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFEAPurple BackGround ":
                qrCode.setBackgroundColor(Color.magenta);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDFEBBrown BackGround ":
                qrCode.setBackgroundColor(new Color(165, 42, 42));
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "⬛️Black BackGround ":
                qrCode.setBackgroundColor(Color.BLACK);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "⬜️White BackGround ":
                qrCode.setBackgroundColor(Color.WHITE);
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "Choose Random color for BackGround":
                qrCode.setBackgroundColor(TgBotUtils.getRandomColor());
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83C\uDF08 Choose Random colors":
                qrCode.setBackgroundColor(TgBotUtils.getRandomColor());
                qrCode.setForegroundColor(TgBotUtils.getRandomColor());
                qrCodeRepository.save(qrCode);
                getMessages().tellUserToWriteTextForQRCode(user);
                break;
            case "\uD83D\uDD04 change link":
                if (user.getRole().equals(Role.VIP)) {
                    user.setWantToChangeLink(true);
                    user.setStepOfManagingCodes(1);
                    userRepository.save(user);
                    getMessages().sendMessageForChangingQRCodeLink(user);
                } else {
                    sendMessageToUser(user, "for changing link you need VIP status...");
                }
                break;
            case "🗑️Delete":
                user.setWantToDelete(true);
                user.setStepOfManagingCodes(1);
                userRepository.save(user);
                getMessages().sendMessageForDeletingQRCode(user);
                break;
            case "👁️Check visitors":
                user.setWantToCheckVisitors(true);
                user.setStepOfManagingCodes(1);
                userRepository.save(user);
                getMessages().sendMessageForCheckingQrCodeVisitors(user);
                break;
            case "⬅️ back":
                processBackButton(user);
                break;
            default:
                // Hans
                break;
        }
    }

    @SneakyThrows
    public void sendPhoto(Long chatId, InputFile qrFile) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(qrFile);
        execute(sendPhoto);
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

    public void deleteMessage(TgUser user, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(user.getChatId().toString());
        deleteMessage.setMessageId(messageId);

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    void processBackButton(TgUser user) {

        QrCode notCreatedQrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user)
                .orElse(null);

        if (user.isWantToDelete()) {
            deleteMessage(user, user.getAdditionalMessageId());
            user.setWantToDelete(false);
            user.setStepOfManagingCodes(0);
            userRepository.save(user);
            getMessages().sendMessageForManagingQrCodes(user);
            return;
        } else if (user.isWantToChangeLink()) {
            deleteMessage(user, user.getAdditionalMessageId());
            user.setWantToChangeLink(false);
            user.setStepOfManagingCodes(0);
            userRepository.save(user);
            getMessages().sendMessageForManagingQrCodes(user);
            return;
        } else if (user.isWantToCheckVisitors()) {
            deleteMessage(user, user.getAdditionalMessageId());
            user.setWantToCheckVisitors(false);
            user.setStepOfManagingCodes(0);
            userRepository.save(user);
            getMessages().sendMessageForManagingQrCodes(user);
            return;
        }

        if (user.getStepOfManagingCodes() == 1) {
            deleteMessage(user, user.getAdditionalMessageId());
            user.setStepOfManagingCodes(0);
            getMessages().sendMessageForManagingQrCodes(user);
        }

        if (notCreatedQrCode != null) {
            if (notCreatedQrCode.getType().equals("basic")) {
                if (user.getStepOfGenerationCode() == 4) {
                    getMessages().showUserCustomizationForBasicQRCode(user);
                    return;
                }
            }
        }

        switch (user.getStepOfGenerationCode()) {
            case 2 -> getMessages().showOptionsToChooseType(user);
            case 20, 3 -> getMessages().showUserPossibleCustomizationForForeground(user);
            case 30, 4 -> getMessages().showUserPossibleCustomizationForBackGround(user);
        }
    }


    @SneakyThrows
    public void generateMenuButtons() {
        List<BotCommand> listOfCommands = List.of(
                new BotCommand("/generateqrcode", "create your QR code"),
                new BotCommand("/showmyqrcodes", "show all your QR codes"),
                new BotCommand("/profile", "open your profile"),
                new BotCommand("/info", "information about this bot")
//                new BotCommand("/buyvip", "Unlock VIP status")


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


