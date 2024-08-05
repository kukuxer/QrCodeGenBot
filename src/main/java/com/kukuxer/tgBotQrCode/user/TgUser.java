package com.kukuxer.tgBotQrCode.user;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "tg_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TgUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long chatId;
    Long telegramUserId;
    String tgUsername;
    @Enumerated(EnumType.STRING)
    Role role;
    Integer messageId;
    int stepOfGenerationCode;
    boolean generateQrCodeRightNow;
    boolean isOnFinalStepOfCreation;
    @OneToMany()
    @JoinColumn(name = "qr_code_id")
    List<QrCode> qrCodes;


}
