package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.user.TgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

    @Query("SELECT q FROM QrCode q WHERE q.creator = :creator AND q.isCreated = false ORDER BY q.creationDate DESC LIMIT 1")
    Optional<QrCode> findByCreatorAndIsCreatedFalse(@Param("creator") TgUser creator);

    List<QrCode> findAllByCreator(TgUser creator);
}
