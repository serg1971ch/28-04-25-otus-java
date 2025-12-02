package ru.otus.minioBot.web.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.repository.NotificationsRepository;
import ru.otus.minioBot.web.dto.NotificationDto;
import ru.otus.minioBot.web.mappers.NotificationMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationDtoService {
    private final NotificationsRepository notificationsRepository;
    private final NotificationMapper notificationMapper;

    public NotificationDtoService(NotificationsRepository notificationsRepository, NotificationMapper notificationMapper) {
        this.notificationsRepository = notificationsRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public List<NotificationDto> findNotificationsByChatId(long chatId) {
        List<Notification> notifications = notificationsRepository.findByChatId(chatId);
        return notificationMapper.toDto(notifications);
    }

    public Optional<NotificationDto> findNotificationById(Long id) {
        return notificationsRepository.findById(id)
                .map(notificationMapper::toDto); // Преобразуем сущность в DTO, если она найдена
    }

    // Вот ответ на ваш вопрос:
    // Метод, который принимает chatId и dateTime и возвращает List<NotificationDto>
    public List<NotificationDto> findNotificationsDtoByChatIdAndSentDateAfter(Long chatId, LocalDateTime dateTime) {
        List<Notification> notifications = notificationsRepository.findByChatIdAndSentDateAfter(chatId, dateTime);
        return notificationMapper.toDto(notifications); // Используем маппер для преобразования в DTO
    }

    public NotificationDto saveNotification(NotificationDto notificationDto) {
        Notification notification = notificationMapper.toEntity(notificationDto);
        Notification savedNotification = notificationsRepository.save(notification);
        return notificationMapper.toDto(savedNotification);
    }

}
