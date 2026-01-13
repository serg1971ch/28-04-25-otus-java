package ru.upmt.webServerBot.listener.messages;

import java.io.File;
import java.nio.file.Files;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;
import ru.upmt.webServerBot.listener.keyboard.KeyboardFactory;
import ru.upmt.webServerBot.listener.processors.RemarkProcessor;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.web.service.NotificationDtoService;
import java.util.Optional;

import static ru.upmt.webServerBot.model.ChatState.IDLE;


@Slf4j
@Component
public class MessageSenderImpl implements MessageSender {

    private final TelegramBot telegramBot;
    private final KeyboardFactory keyboardFactory;
    private final StateManager stateManager;
    private final RemarkProcessor remarkProcessor;

    @Autowired
    public MessageSenderImpl(TelegramBot telegramBot, KeyboardFactory keyboardFactory, StateManager stateManager,RemarkProcessor remarkProcessor) {
        this.telegramBot = telegramBot;
        this.keyboardFactory = keyboardFactory;
        this.stateManager = stateManager;
        this.remarkProcessor = remarkProcessor;
    }

    public void sendMessage(long chatId, String text) {
        telegramBot.execute(new SendMessage(chatId, text));
    }

    public void sendWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage request = new SendMessage(chatId, text).replyMarkup(keyboard);
        telegramBot.execute(request);
    }

    public void sendWelcomeMessage(long chatId, Message message) {
        sendMessage(chatId, "Привет, " + message.from().firstName() + " Я бот для работы с фотографиями и замечаниями.");
        keyboardFactory.sendMainMenuBot(chatId);
        stateManager.updateState(chatId, IDLE);
    }

    public void sendPositionsMenu(long chatId) {
        InlineKeyboardMarkup markup = keyboardFactory.createPaginatedKeyboardForPositions(1, 16);
        telegramBot.execute(
                new SendMessage(chatId, "Выберите позицию:")
                        .replyMarkup(markup)
        );
    }

    public void sendPositionNotification(long chatId, Optional<Notification> notification, String position) {
        notification.ifPresent(remark -> {
            remark.setPosition("поз. " + position);
            remarkProcessor.updatePositionNotification(remark);
        });
    }
}



