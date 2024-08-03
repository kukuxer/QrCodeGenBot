package com.kukuxer.tgBotQrCode.user;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "tg_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TgUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private Long telegramUserId;
    private String tgUsername;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany
    @JoinColumn(name = "qr_code_id")
    private List<QrCode> qrCodes;

}
