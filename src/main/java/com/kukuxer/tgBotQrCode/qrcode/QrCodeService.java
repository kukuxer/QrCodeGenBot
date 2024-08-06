package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.tgBot.QRCodeTgBot;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;
    private final QRCodeTgBot qrCodeTgBot;
    private final QRCodeGenerator qrCodeGenerator;

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
                .qrCodeVisitors(new ArrayList<>())
                .qrCodeScanCount(0)
                .build();
        qrCodeRepository.save(qrCode);
        user.setStepOfGenerationCode(1);
        user.getQrCodes().add(qrCode);
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
            fullLink = "http://localhost:8080/redirect/" + qrCode.getUuid();
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
        user.setStepOfGenerationCode(10);
        user.setOnFinalStepOfCreation(false);
        user.setGenerateQrCodeRightNow(false);
        user.setMessageId(null);
        userRepository.save(user);
    }

    public List<QrCode> getQrCodesByUser(TgUser user) {
        return qrCodeRepository.findAllByCreator(user);
    }
}
