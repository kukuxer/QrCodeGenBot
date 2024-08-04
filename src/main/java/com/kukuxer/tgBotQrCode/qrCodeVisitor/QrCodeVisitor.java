package com.kukuxer.tgBotQrCode.qrCodeVisitor;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "qr_code_visitor")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrCodeVisitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String ip;
    String country;
    String city;
    @ManyToOne
    @JoinColumn(name = "visited_qr_code")
    QrCode visitedQrCode;

}

