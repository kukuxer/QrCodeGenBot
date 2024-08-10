package com.kukuxer.tgBotQrCode.mono;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonoAccount {

    String clientId;
    String name;
    String webHookUrl;
    String permissions;
    List<Account> accounts;
    List<Jar> jars;
}
