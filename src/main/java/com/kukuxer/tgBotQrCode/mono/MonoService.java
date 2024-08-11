package com.kukuxer.tgBotQrCode.mono;

import com.kukuxer.tgBotQrCode.tgBot.QRCodeTgBot;
import com.kukuxer.tgBotQrCode.user.Role;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonoService {

    private final UserRepository userRepository;

    private final RestTemplate restTemplate;

    private final QRCodeTgBot qrCodeTgBot;
    @Value("${mono.token}")
    private String monoToken;

    public ResponseEntity<MonoAccount> getPersonalInfo() {
        HttpEntity<String> entity = getStringHttpEntity();

        return restTemplate.exchange(
                "https://api.monobank.ua/personal/client-info",
                HttpMethod.GET,
                entity,
                MonoAccount.class
        );
    }

    @Scheduled(fixedRate = 60000)
    public ResponseEntity<List<Transaction>> getTransactions() {
        HttpEntity<String> entity = getStringHttpEntity();

        long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - 6000;
        log.debug("Requesting transactions from Unix timestamp: {}", time);

        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
                "https://api.monobank.ua/personal/statement/CLZtIPW9zHvyXBJG8xfPZg/" + time,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        List<Transaction> transactions = response.getBody();
        List<TgUser> users = userRepository.findAll();

        if (transactions != null) {
            transactions.forEach(t -> setVips(t, users));
        } else log.info("no transactions last minutes");
        log.info(transactions.size() + " transactions last 10 munites");
        return response;
    }
 /*

     твои комменты глеб ->
             Transaction transaction = transactions.get(0);
            String comment = transaction.getComment();
            TgUser tgUser = userRepository.findByChatId(
                    Long.parseLong(comment)
            ).orElseThrow(
                    () -> new RuntimeException("User with telegram id: " + comment + " wasn't found.")
            );
            long amount = transaction.getAmount();
            if (amount > 499L) {
                setVip(tgUser);
            } else {
                long fullM = amount / 100;
                long notFullM = amount % 100;
                qrCodeTgBot.sendMessageToUser(
                        tgUser,
                        "Bro, you've sent insufficient amount of money: " + fullM + "UAH" + notFullM + "kop." + ", but u got VIP anyway" +
                                "You owe to us: " + (500L - amount)
                );
            }
    */

    @NotNull
    private HttpEntity<String> getStringHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Token", monoToken);
        return new HttpEntity<>(headers);
    }

//    private void setVip(TgUser tgUser) {
//        tgUser.setRole(Role.VIP);
//       userRepository.save(tgUser);
//        qrCodeTgBot.sendMessageToUser(tgUser, "VIP VIP VIP");
//    }

    private void setVips(Transaction t, List<TgUser> users) {
        String comment = t.getComment();
        long amount = t.getAmount();
        try {
            Long userId = Long.parseLong(comment);
            users.forEach(user -> {
                if (user.getChatId().equals(userId) && user.getRole().equals(Role.USER)) {
                    user.setRole(Role.VIP);
                    sendThankYouMessage(user, amount);
                    log.info("user " + user.getTgUsername() + " bought a VIP status");
                    userRepository.save(user);
                }
            });
        } catch (NumberFormatException e) {
            log.warn("Failed to parse user ID from comment: '{}'. Error: {}", comment, e.getMessage());
        }
    }

    private void sendThankYouMessage(TgUser user, long amount) {
        long fullM = amount / 100;
        long notFullM = amount % 100;

        String message = String.format("Тепер ви VIP. Ви надіслали %dгрн %dкоп. Ці гроші будуть переведені на ЗСУ. Дякуємо за допомогу! 🇺🇦", fullM, notFullM);
        qrCodeTgBot.sendMessageToUser(user, message);
    }


}
