package com.kukuxer.tgBotQrCode.qrCodeVisitor;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import com.kukuxer.tgBotQrCode.qrcode.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QrCodeVisitorService {
    private final QrCodeVisitorRepository qrCodeVisitorRepository;
    private final GeolocationService geolocationService;

    @Transactional
    public QrCodeVisitor createQrCodeVisitorByIpOrElseCreateNew(String ip, QrCode qrCode) {
        return qrCodeVisitorRepository.findByIpAndVisitedQrCode(ip, qrCode)
                .orElseGet(() -> {
                    String city = geolocationService.getCityByIp(ip);
                    String country = geolocationService.getCountryByIp(ip);
                    QrCodeVisitor newVisitor = QrCodeVisitor.builder()
                            .ip(ip)
                            .country(country)
                            .city(city)
                            .visitedQrCode(qrCode)
                            .build();
                    return qrCodeVisitorRepository.save(newVisitor);
                });
    }
}
