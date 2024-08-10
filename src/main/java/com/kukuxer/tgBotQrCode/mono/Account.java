package com.kukuxer.tgBotQrCode.mono;

import lombok.Data;

import java.util.List;
@Data

public class Account {
    String id;
    String sendId;
    Integer balance;
    Long creditLimit;
    String type;
    Integer currencyCode;
    String cashbackType;
    List<Object> maskedPan;
    String iban;

}
