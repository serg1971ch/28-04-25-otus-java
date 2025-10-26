package ru.otus.minioBot.service;


import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.IncorrectMessageException;
import ru.otus.minioBot.model.Notification;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface NotificationService {

    void sendNotificationMessage();

    long scheduleNotification(LocalDateTime notification, long chatId);

    Optional<LocalDateTime> parseMessage(String message) throws IncorrectMessageException;

    void sendMessage(long chatId, String message);

    void sendMessage(Notification notification);

    String processRemarkInput(int chatId, String commandAndData) throws IncorrectMessageException;

    String processRemarkInputPhoto(long chatId,  String description, int position, boolean isYes);

//    void sendMessage(Notification notification);
}
