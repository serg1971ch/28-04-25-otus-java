package ru.upmt.webServerBot.listener.handlers;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;
import ru.upmt.webServerBot.listener.messages.MessageSenderImpl;
import ru.upmt.webServerBot.listener.processors.RemarkProcessor;
import ru.upmt.webServerBot.model.ChatState;
import ru.upmt.webServerBot.model.ExecuteNotification;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.service.ExecuteNotificationService;
import ru.upmt.webServerBot.service.UserService;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.upmt.webServerBot.CommandConst.COMMAND_START;
import static ru.upmt.webServerBot.model.ChatState.AWAITING_PHOTO_FOR_ADD_REMARK;
import static ru.upmt.webServerBot.model.ChatState.AWAITING_POSITION_NAME;


@Slf4j
@Component
public class MessageHandler {
    private final StateManager stateManager;
    private final RemarkProcessor remarkProcessor;
    private final MessageSenderImpl messageSender;
    private final PhotoHandler photoHandler;
    private final ExecuteNotificationService executeNotificationService;
    @Getter
    private Optional<Notification> notification;
    private final UserService userService;

    @Autowired
    public MessageHandler(StateManager stateManager,
                          RemarkProcessor remarkProcessor,
                          MessageSenderImpl messageSender, PhotoHandler photoHandler, ExecuteNotificationService executeNotificationService, UserService userService) {
        this.stateManager = stateManager;
        this.remarkProcessor = remarkProcessor;
        this.messageSender = messageSender;
        this.photoHandler = photoHandler;
        this.executeNotificationService = executeNotificationService;
        this.userService = userService;
    }

    public void handle(Message message) throws IOException, URISyntaxException {
        long chatId = message.chat().id();
        ChatState currentState = stateManager.getCurrentState(chatId);

        if (message.text() != null) {
            handleText(message, chatId, currentState);
        } else if (message.photo() != null && message.photo().length > 0) {
            handlePhoto(message, currentState);
        }
    }

    private void handleText(Message message, long chatId, ChatState state) {
        if (notification != null) {
            notification = remarkProcessor.getCurrentRemark(chatId);
        }

        switch (state) {
            case AWAITING_TEXT_FOR_ADD_REMARK -> {
                notification = Optional.ofNullable(remarkProcessor.process(message.text(), chatId));
                log.info("notification after process method: {}", notification.get().getComment());
                stateManager.updateState(chatId, AWAITING_POSITION_NAME);
                log.info("state in awaiting_text_for_add_photo: {}", stateManager.getCurrentState(chatId));
                messageSender.sendPositionsMenu(chatId);
            }

            case AWAITING_PHOTO -> {
                messageSender.sendMessage(chatId, "Выберите фото вашего замечания: " + notification.get().getComment() + " после его устранения");
                log.info("Здесь заканчивается передача фото");
//                    sendExistMenuButtons(chatId);
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
            }

            case AWAITING_POSITION_NAME -> {
                log.info("current state^: {}", stateManager.getCurrentState(chatId));
                log.info("current message text^: {}", message.text());
                messageSender.sendMessage(chatId, "Позиция" + notification.get()
                        .getPosition() + " к замечанию " + notification.get().getComment() + " добавлена ");
            }

            case AWAITING_OPERATOR_EXECUTED -> {
                log.info("current state^: {}", stateManager.getCurrentState(chatId));
//                log.info("current message text : {}", message.text());
                List<String> names = List.of(message.text().split(","));
                List<User> users = getUsers(names.toString(), chatId);
                for (User user : users) {
                    executeNotificationService.saveNotification(user.getLastName(), notification.get());
                }
                stateManager.updateState(chatId, AWAITING_PHOTO_FOR_ADD_REMARK);
                log.info("current state^: {}", stateManager.getCurrentState(chatId));
                messageSender.sendMessage(chatId, "\uD83E\uDEE0   Выберите фото вашего замечания: "
                        + notification.get().getComment() + " после его устранения");
            }

            case IDLE -> {
                if (COMMAND_START.equals(message.text())) {
                    messageSender.sendWelcomeMessage(chatId, message);
                    stateManager.resetState(chatId);
                } else {
                    messageSender.sendMessage(chatId, "Используйте меню.");
                }
            }
            default -> messageSender.sendMessage(chatId, "Ожидаю действия через меню.");
        }
    }

    private void handlePhoto(Message message, ChatState state) throws IOException, URISyntaxException {
        long chatId = message.chat().id();
        if (state == AWAITING_PHOTO_FOR_ADD_REMARK) {
            photoHandler.handle(message);
        } else {
            log.info("данное состояние бот {}:", stateManager.getCurrentState(chatId).name());
            messageSender.sendMessage(chatId, "Фото не ожидается. Используйте меню.");
        }
    }

    private List<User> getUsers(String text, long chatId) {
        List<User> users = new ArrayList<>();
        String[] userEntries = text.split(", *");

        for (String entry : userEntries) {
            log.info("entry: {}", entry);
            if(entry.startsWith("[")) {
                entry = entry.replace("[", "");
            } else if(entry.endsWith("]")) {
                entry = entry.replace("]", "");
            } else {
                messageSender.sendMessage(chatId, "Повторите фамилии исполнителей через запятую, проверьте корректность заполнения списка фамилий");
            }
            log.info("entry: {}", entry);
            User user = userService.getUser(entry);
            users.add(user);
        }
        return users;
    }
}
