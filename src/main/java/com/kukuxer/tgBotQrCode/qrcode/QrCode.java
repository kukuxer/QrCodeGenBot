package com.kukuxer.tgBotQrCode.qrcode;

import com.kukuxer.tgBotQrCode.qrCodeVisitor.QrCodeVisitor;
import com.kukuxer.tgBotQrCode.user.TgUser;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "qr_code")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrCode {

    @Id
    @UuidGenerator
    UUID uuid;

    String text;
    String fullLink;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    TgUser creator;
    String foregroundColor;
    String backgroundColor;
    String type;
    @CreationTimestamp
    @JoinColumn(name = "creation_date")
    LocalDateTime creationDate;
    @JoinColumn(name = "expiration_time")
    LocalDateTime expirationTime;
    @JoinColumn(name = "is_active")
    Boolean isActive;
    Boolean isCreated;
    Integer qrCodeScanCount;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "qr_code_visitor_id")
    List<QrCodeVisitor> qrCodeVisitors;
    @Lob
    byte[] qrCodeImage;

    public void setForegroundColor(Color color) {
        this.foregroundColor = color != null ? "#" + Integer.toHexString(color.getRGB()).substring(2) : null;
    }

    public Color getForegroundColor() {
        return foregroundColor != null ? Color.decode(foregroundColor) : null;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color != null ? "#" + Integer.toHexString(color.getRGB()).substring(2) : null;
    }

    public Color getBackgroundColor() {
        return backgroundColor != null ? Color.decode(backgroundColor) : null;
    }

}
