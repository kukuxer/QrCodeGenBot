package com.kukuxer.tgBotQrCode.mono;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {

    String id;
    long time;
    String description;
    int mcc;
    int originalMcc;
    boolean hold;
    long amount;
    long operationAmount;
    int currencyCode;
    int commissionRate;
    long cashbackAmount;
    long balance;
    String comment;
    String receiptId;
    String invoiceId;
    String counterEdrpou;
    String counterIban;
    String counterName;
}
