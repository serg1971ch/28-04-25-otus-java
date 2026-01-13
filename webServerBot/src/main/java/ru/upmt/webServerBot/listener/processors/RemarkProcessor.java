package ru.upmt.webServerBot.listener.processors;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.listener.StateManager;
import ru.upmt.webServerBot.listener.messages.MessageSenderImpl;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.service.NotificationService;


import java.util.Optional;

import static ru.upmt.webServerBot.CommandConst.INVALID_MSG;

@Component
@Slf4j
public class RemarkProcessor implements RemarkReceiver {

    private final NotificationService notificationService;
    private final StateManager stateManager;
    private final MessageSenderImpl messageSender;
    private Long notificationId;
    Notification notification;

    // Конструктор с внедрением зависимостей
    @Autowired
    public RemarkProcessor(NotificationService notificationService,
                           StateManager stateManager,
                           @Lazy MessageSenderImpl messageSender) {
        this.notificationService = notificationService;
        this.stateManager = stateManager;
        this.messageSender = messageSender;
    }

    /**
     * Обрабатывает ввод замечания от пользователя
     *
     * @param input  текст введенного замечания
     * @param chatId идентификатор чата
     */
    public Notification process(String input, long chatId) {

        if (input == null || input.trim().isEmpty()) {
            messageSender.sendMessage(chatId, INVALID_MSG);
        }

        String trimmedInput = input.trim();

        try {
            notification = notificationService.processRemarkInput(chatId, trimmedInput);
            notificationId = notification.getId();
            messageSender.sendMessage(
                    chatId,
                    notification.getComment().replace(";", "") +
                            " добавлено. Теперь отметьте позицию " +
                            "этого замечания."
            );
            log.info("Remark processed for chat {}. State updated to: {}.", chatId, stateManager.getCurrentState(chatId));
        } catch (Exception e) {
            log.error("Error processing remark from chat {}: {}", chatId, e.getMessage(), e);
            messageSender.sendMessage(chatId, "Произошла ошибка при обработке замечания. Попробуйте позже.");
        }
        return notification;
    }

    /**
     * Получает текущее замечание для чата (если есть)
     *
     * @param chatId идентификатор чата
     * @return текущее замечание или null
     */
    public Optional<Notification> getCurrentRemark(long chatId) {
        Optional<Notification> notification = Optional.empty();
        if (notificationId != null) {
            notification = notificationService.findNotificationById(notificationId);
        }
        return notification;
    }

    @Transactional
    public void updatePositionNotification(Notification notification) {
        long id = notification.getId();
        String position = notification.getPosition();
        notificationService.update(id, position);
        log.info("Notification saved with id={}", notification.getId());
    }
}
