package ru.upmt.webServerBot.service;


import org.springframework.stereotype.Service;
import ru.upmt.webServerBot.exceptions.IncorrectMessageException;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.web.dto.RemarkWithImageDTO;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface NotificationService {

    void sendNotificationMessage(long chatId);

    long scheduleNotification(LocalDateTime notification, long chatId);

    Optional<LocalDateTime> parseMessage(String message) throws IncorrectMessageException;

    Long getIdNotification(Notification notification);

//    Optional<Notification> getCurrentNotificationForChat(Long chatId, Long notificationId);

    RemarkWithImageDTO getRemarksForChat(long chatId, long noteId);

    void sendMessage(long chatId, String message);

    void sendMessage(Notification notification);

    Notification processRemarkInput(Long chatId, String commandAndData) throws IncorrectMessageException;

    Optional<Notification> findNotificationById(long id);

    void save(Notification notification);

    void update(Long id, String position);
}
