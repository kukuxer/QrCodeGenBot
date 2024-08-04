package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitor;
import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class QrCodeController {
    private final QrCodeRepository qrCodeRepository;
    private final QrCodeVisitorService qrCodeVisitorService;

    @GetMapping("/redirect/{qrCodeId}")
    public ResponseEntity<?> redirect(@PathVariable("qrCodeId") String qrCodeIdString, HttpServletRequest request) {
        try {
            UUID qrCodeId = UUID.fromString(qrCodeIdString);

            QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                    .orElseThrow(() -> new RuntimeException("QR code not found"));

            String ipAddress = request.getRemoteAddr();
            QrCodeVisitor qrCodeVisitor = qrCodeVisitorService.createQrCodeVisitorByIpOrElseCreateNew(ipAddress, qrCode);

            qrCode.getQrCodeVisitors().add(qrCodeVisitor);
            qrCode.setQrCodeScanCount(qrCode.getQrCodeScanCount() + 1);
            qrCodeRepository.save(qrCode);

            String url = qrCode.getText();
            URI redirectUri = new URI(url);

            if (isValidUrl(url)) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUri.toString())
                        .build();
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body("QR Code Text: " + qrCode.getText());
            }
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid URL: " + qrCodeIdString);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body("QR Code Text: " + qrCodeIdString);
        }
    }
    private boolean isValidUrl(String url) {
        try {
            new URI(url).parseServerAuthority();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
}
}
