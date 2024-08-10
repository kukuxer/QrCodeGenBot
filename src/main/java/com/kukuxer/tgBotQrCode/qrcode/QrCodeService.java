package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitor;
import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitorRepository;
import com.kukuxer.tgBotQrCode.tgBot.MessagesForUser;
import com.kukuxer.tgBotQrCode.tgBot.QRCodeTgBot;
import com.kukuxer.tgBotQrCode.user.Role;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;
    private final QRCodeTgBot qrCodeTgBot;
    private final QRCodeGenerator qrCodeGenerator;
    private final MessagesForUser messages;
    private final QrCodeVisitorRepository qrCodeVisitorRepository;

    @Transactional
    public void generateQrCode(TgUser user) {
        if (user.isGenerateQrCodeRightNow()) {
            throw new RuntimeException("aboba");
        }

        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        QrCode qrCode = QrCode.builder()
                .creator(user)
                .backgroundColor("#FFFFFF")
                .foregroundColor("#000000")
                .isActive(false)
                .isCreated(false)
                .type("none")
                .qrCodeVisitors(new ArrayList<>())
                .qrCodeScanCount(0)
                .build();
        qrCodeRepository.save(qrCode);
        user.setStepOfGenerationCode(1);
        userRepository.save(user);

    }

    @SneakyThrows
    public void generateQrCode(TgUser user, Update update) {
        QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(() -> new RuntimeException("QR Code not found for user"));
        String text = update.getMessage().getText();
        String fullLink;


        if (qrCode.getType().equals("raw")) {
            fullLink = update.getMessage().getText();
        } else {
            fullLink = "https://qrcodegenbot.onrender.com/" + qrCode.getUuid();
        }

        log.info("link: " + fullLink);
        InputFile qrFile = qrCodeGenerator.getQRCodeImageFile(fullLink, qrCode.getForegroundColor(), qrCode.getBackgroundColor());
        qrCodeTgBot.sendPhoto(update.getMessage().getChatId(), qrFile);

        qrCode.setIsCreated(true);
        qrCode.setIsActive(true);
        qrCode.setQrCodeScanCount(0);
        qrCode.setText(text);
        qrCode.setFullLink(fullLink);
        qrCodeRepository.save(qrCode);
        if (text.equals(String.valueOf(user.getSecretCode()))) {
            user.setRole(Role.VIP);
            qrCodeTgBot.sendMessageToUser(user,"VIP VIP VIP");
        }
        user.setStepOfGenerationCode(10);
        user.setOnFinalStepOfCreation(false);
        user.setGenerateQrCodeRightNow(false);
        user.setMessageId(null);
        userRepository.save(user);
    }

    public void setCustomColorForForeGround(TgUser user, Update update) {
        try {
            QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(() -> new RuntimeException("QR Code not found for user"));
            qrCode.setForegroundColor(Color.decode(update.getMessage().getText()));
            qrCodeRepository.save(qrCode);
            messages.showUserPossibleCustomizationForBackGround(user);
        } catch (Exception e) {
            e.printStackTrace();
            qrCodeTgBot.sendMessageToUser(user, "something went wrong, try again(probably wrong format)");
        }
    }

    public void setCustomColorForBackGround(TgUser user, Update update) {
        try {
            QrCode qrCode = qrCodeRepository.findByCreatorAndIsCreatedFalse(user).orElseThrow(() -> new RuntimeException("QR Code not found for user"));
            qrCode.setBackgroundColor(Color.decode(update.getMessage().getText()));
            qrCodeRepository.save(qrCode);
            messages.tellUserToWriteTextForQRCode(user);
        } catch (Exception e) {
            e.printStackTrace();
            qrCodeTgBot.sendMessageToUser(user, "something went wrong, try again(probably wrong format)");
        }
    }

    public void processQRCodeGenerating(TgUser user) {
        if (user.isGenerateQrCodeRightNow()) {
            qrCodeTgBot.sendMessageToUser(user, "Sorry but you already generating Qr code!");
            return;
        }
        generateQrCode(user);
        messages.showOptionsToChooseType(user);
    }

    public List<QrCode> getQrCodesByUser(TgUser user) {
        return qrCodeRepository.findAllByCreator(user);
    }

    public void deleteQrCode(TgUser user, String text) {
        try {
            QrCode qrCode = qrCodeRepository.findById(UUID.fromString(text)).orElseThrow(
                    () -> new RuntimeException("Qr not found by id" + text)
            );
            qrCodeRepository.delete(qrCode);
            qrCodeTgBot.sendMessageToUser(user, " Qr code was successfully deleted \uD83E\uDD2B \uD83E\uDD2B \uD83E\uDD2B ");
            user.setWantToDelete(false);
            userRepository.save(user);

        } catch (Exception e) {
            qrCodeTgBot.sendMessageToUser(user, "no QR codes exists by this id");
        }
    }

    public void showQRCodeVisitorsById(TgUser user, String text) {
        try {
            QrCode qrCode = qrCodeRepository.findById(UUID.fromString(text)).orElseThrow(
                    () -> new RuntimeException("Qr not found by id" + text)
            );
            List<QrCodeVisitor> qrCodeVisitors = qrCodeVisitorRepository.findAllByVisitedQrCode(qrCode);
            if(user.getRole().equals(Role.VIP)){
                messages.showUserQrCodeVisitors(user, qrCodeVisitors);
            }else {
                messages.showUserQrCodeVisitorsNoVIP(user,qrCodeVisitors);
            }

            user.setWantToCheckVisitors(false);
            userRepository.save(user);

        } catch (Exception e) {
            qrCodeTgBot.sendMessageToUser(user, "no QR codes exists by this id");
        }
    }

    public void processChangeQrCodeLink(TgUser user, String data) {
        try {
            QrCode qrCode = qrCodeRepository.findById(UUID.fromString(data)).orElseThrow();
            user.setQrCodeIdToChange(qrCode.getUuid());
            userRepository.save(user);
            qrCodeTgBot.sendMessageToUser(user, " Current text: " + qrCode.getText() + " Please provide new link or text for your qr code: ");
        } catch (Exception e) {
            log.warn(e.getMessage() +" while processChangeQrCodeLink");
        }
    }

    public void changeQrCodeLink(TgUser user, String text) {
        try {
            QrCode qrCode = qrCodeRepository.findById(user.getQrCodeIdToChange()).orElseThrow();
            qrCode.setText(text);
            qrCodeRepository.save(qrCode);
            user.setWantToChangeLink(false);
            user.setQrCodeIdToChange(null);
            userRepository.save(user);
            qrCodeTgBot.sendMessageToUser(user, "Successfully changed to " + text);
            qrCodeTgBot.getMessages().showQrCode(user, qrCode);

        } catch (Exception e) {
            qrCodeTgBot.sendMessageToUser(user, "Ooooopss something wrong");
        }
    }
}
