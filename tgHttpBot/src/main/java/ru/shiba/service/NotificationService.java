package ru.shiba.service;


import org.springframework.stereotype.Service;
import ru.shiba.exceptions.IncorrectMessageException;
import ru.shiba.model.Notification;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface NotificationService {

    void sendNotificationMessage();

    long scheduleNotification(LocalDateTime notification, long chatId);

    Optional<LocalDateTime> parseMessage(String message) throws IncorrectMessageException;

    void sendMessage(long chatId, String message);

    void sendMessage(Notification notification);

    String processRemarkInput(int chatId, String commandAndData);

//    void sendMessage(Notification notification);
}
