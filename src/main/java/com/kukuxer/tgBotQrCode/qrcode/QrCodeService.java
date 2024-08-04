package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void generateQrCode(TgUser user) {

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

        user.getQrCodes().add(qrCode);
        user.setGenerateQrCodeRightNow(true);

        userRepository.save(user);

    }
}
