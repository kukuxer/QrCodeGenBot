package com.kukuxer.tgBotQrCode.tgBot;

import com.kukuxer.tgBotQrCode.tgBot.QRCodeTgBot;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Getter
@Configuration
@EnableTransactionManagement
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotsApi(QRCodeTgBot telegramBot) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
        return telegramBotsApi;
    }

}
