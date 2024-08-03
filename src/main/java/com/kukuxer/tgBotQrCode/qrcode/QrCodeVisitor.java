package com.kukuxer.tgBotQrCode.qrcode;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "qr_code_visitor")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrCodeVisitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String ip;
    String country;
    @ManyToOne
    @JoinColumn(name = "visited_qr_code")
    QrCode visitedQrCode;

}

