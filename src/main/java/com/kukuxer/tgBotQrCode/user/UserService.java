package com.kukuxer.tgBotQrCode.user;

import com.kukuxer.tgBotQrCode.qrcode.QrCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    @Transactional
    public TgUser getByChatIdOrElseCreateNew(Update update) {
        Long chatId = extractChatIdFromUpdate(update);
        Optional<TgUser> optionalUser = userRepository.findByChatId(chatId);

        return optionalUser.orElseGet(() -> createUserFromUpdate(update, chatId));
    }

    @Transactional
    public List<QrCode> getUserQrCodes(TgUser user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getQrCodes();
    }

    private TgUser createUserFromUpdate(Update update, Long chatId) {
        Message message = update.getMessage();

        String username = Optional.ofNullable(message.getFrom().getUserName())
                .orElse("Unknown" + message.getFrom().getId());

        TgUser newUser = TgUser.builder()
                .chatId(chatId)
                .tgUsername(username)
                .telegramUserId(message.getFrom().getId())
                .qrCodes(new ArrayList<>())
                .generateQrCodeRightNow(false)
                .isOnFinalStepOfCreation(false)
                .secretCode(ThreadLocalRandom.current().nextInt(100, 1000))
                .stepOfGenerationCode(0)
                .role(Role.USER)
                .build();

        return userRepository.save(newUser);
    }

    private Long extractChatIdFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else {
            throw new IllegalArgumentException("Update does not contain a valid chat ID");
        }
    }


}
