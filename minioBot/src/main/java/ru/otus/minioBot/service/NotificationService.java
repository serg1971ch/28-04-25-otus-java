package ru.otus.minioBot.service;


import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.IncorrectMessageException;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.web.dto.RemarkWithImageDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface NotificationService {

    void sendNotificationMessage(long chatId);

    long scheduleNotification(LocalDateTime notification, long chatId);

    Optional<LocalDateTime> parseMessage(String message) throws IncorrectMessageException;

    Long getIdNotification(Notification notification);

    RemarkWithImageDTO getRemarksForChat(long chatId, long noteId);

    void sendMessage(long chatId, String message);

    void sendMessage(Notification notification);

    Notification processRemarkInput(Long chatId, String commandAndData) throws IncorrectMessageException;
}
