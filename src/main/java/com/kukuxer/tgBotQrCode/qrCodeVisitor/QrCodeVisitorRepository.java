package com.kukuxer.tgBotQrCode.qrCodeVisitor;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrCodeVisitorRepository extends JpaRepository<QrCodeVisitor, Long> {
    Optional<QrCodeVisitor> findByIpAndVisitedQrCode(String ip, QrCode visitedQrCode);
}
