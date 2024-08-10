package com.kukuxer.tgBotQrCode.mono;

import com.kukuxer.tgBotQrCode.tgBot.QRCodeTgBot;
import com.kukuxer.tgBotQrCode.user.Role;
import com.kukuxer.tgBotQrCode.user.TgUser;
import com.kukuxer.tgBotQrCode.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
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

    public ResponseEntity<List<Transaction>> getTransactions() {
        HttpEntity<String> entity = getStringHttpEntity();

        long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)-2592000;
        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
                "https://api.monobank.ua/personal/statement/CLZtIPW9zHvyXBJG8xfPZg/" + time,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        List<Transaction> transactions = response.getBody();
        Transaction transaction = transactions.get(0);
        List<TgUser> users = userRepository.findAll();
        transactions.forEach(t -> {
            setVips(t, users);
        });
        userRepository.saveAll(users);
        String comment = transaction.getComment();
        TgUser tgUser = userRepository.findByChatId(
                Long.parseLong(comment)
        ).orElseThrow(
                () -> new RuntimeException("User with telegram id: " + comment + " wasn't found.")
        );
        long amount = transaction.getAmount();
        if(amount >499L) {
            setVip(tgUser);
        }else{
            long fullM = amount/100;
            long notFullM = amount%100;
            qrCodeTgBot.sendMessageToUser(
                    tgUser,
                    "Bro, you've sent insufficient amount of money: "+fullM+"UAH"+notFullM+"kop."+", but u got VIP anyway"+
                         "You owe to us: "+(500L-amount)
            );
        }
        return response;
    }

    @NotNull
    private HttpEntity<String> getStringHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Token", monoToken);
        return new HttpEntity<>(headers);
    }

    private void setVip(TgUser tgUser) {
        tgUser.setRole(Role.VIP);
//        userRepository.save(tgUser);
        qrCodeTgBot.sendMessageToUser(tgUser, "VIP VIP VIP");
    }

    private static void setVips(Transaction t, List<TgUser> users) {
        String comment = t.getComment();
        try {
            Long userId = Long.parseLong(comment);
            users.forEach(u -> {
                if(u.getId().equals(userId) && u.getRole().equals(Role.USER)){
                    u.setRole(Role.VIP);
                }
            });
        }catch(Exception e){
        }
    }


}
