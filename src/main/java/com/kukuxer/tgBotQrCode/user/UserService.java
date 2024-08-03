package com.kukuxer.tgBotQrCode.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public TgUser getByChatIdOrElseCreateNew(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        return userRepository.findByChatId(chatId).orElseGet(() -> {

            TgUser newPlayer = TgUser.builder()
                    .chatId(chatId)
                    .tgUsername(
                            message.getFrom().getUserName() != null
                                    ? message.getFrom().getUserName()
                                    : "Unknown"+message.getFrom().getId()
                    )
                    .telegramUserId(update.getMessage().getFrom().getId())
                    .qrCodes(new ArrayList<>())
                    .role(Role.USER)
                    .build();
            return userRepository.save(newPlayer);
        });
    }


}
