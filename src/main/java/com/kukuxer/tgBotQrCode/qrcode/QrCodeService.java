package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;

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

    public List<QrCode> getQrCodesByUser(TgUser user) {
        return qrCodeRepository.findAllByCreator(user);
    }
}
