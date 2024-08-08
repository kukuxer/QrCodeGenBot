package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitor;
import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
@RequestMapping()
public class QrCodeController {
    private final QrCodeRepository qrCodeRepository;
    private final QrCodeVisitorService qrCodeVisitorService;

    @GetMapping("/{qrCodeId}")
    public String redirect(@PathVariable("qrCodeId") String qrCodeIdString, HttpServletRequest request, Model model) {
        try {
            UUID qrCodeId = UUID.fromString(qrCodeIdString);

            QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                    .orElseThrow(() -> new RuntimeException("QR code not found"));

            if (qrCode.getExpirationTime().isBefore(LocalDateTime.now())) {
                model.addAttribute("qrCode", qrCode.getQrCodeScanCount());
                model.addAttribute("expirationDate", qrCode.getExpirationTime());
                model.addAttribute("creationDate", qrCode.getCreationDate());
                model.addAttribute("creatorNickname", qrCode.getCreator().getTgUsername());
                return "expired";
            }

            String ipAddress = getClientIp(request);
            QrCodeVisitor qrCodeVisitor = qrCodeVisitorService.createQrCodeVisitorByIpOrElseCreateNew(ipAddress, qrCode);

            qrCode.getQrCodeVisitors().add(qrCodeVisitor);
            qrCode.setQrCodeScanCount(qrCode.getQrCodeScanCount() + 1);
            qrCodeRepository.save(qrCode);

            if ("Russia".equalsIgnoreCase(qrCodeVisitor.getCountry())) {
                model.addAttribute("ipAddress", qrCodeVisitor.getIp());
                model.addAttribute("city", qrCodeVisitor.getCity());
                return "russkiy_idi_nahui";
            }

            String url = qrCode.getText();
            URI redirectUri;
            try {
                redirectUri = new URI(url);
                if (!redirectUri.isAbsolute() || redirectUri.getScheme() == null) {
                    throw new URISyntaxException(url, "Invalid URL scheme");
                }
            } catch (URISyntaxException e) {
                model.addAttribute("message", url);
                return "message";
            }

            return "redirect:" + url;

        } catch (Exception e) {
            model.addAttribute("message", "Error processing QR Code: " + e.getMessage());
            return "message";
        }
    }
    public String getClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header == null || header.isEmpty() || "unknown".equalsIgnoreCase(header)) {
            return request.getRemoteAddr();
        }
        return header.split(",")[0];  // In case of multiple proxies, the first IP is the client's real IP
    }
}
