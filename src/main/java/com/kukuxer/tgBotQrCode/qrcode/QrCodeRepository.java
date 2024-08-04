package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.user.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
    Optional<QrCode> findByCreatorAndIsCreatedFalse(TgUser creator);
}
