package com.kukuxer.tgBotQrCode.mono;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
@Data

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Jar {

    String id;
    String sendId;
    String title;
    String description;
    Integer currencyCode;
    String cashbackType;
    List<Object> maskedPan;
    String iban;

}
