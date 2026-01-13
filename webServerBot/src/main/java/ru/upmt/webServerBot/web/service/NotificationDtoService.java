package ru.upmt.webServerBot.web.service;

import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.repository.NotificationsRepository;
import ru.upmt.webServerBot.repository.UserRepository;
import ru.upmt.webServerBot.service.UserService;
import ru.upmt.webServerBot.web.dto.NotificationDto;
import ru.upmt.webServerBot.web.dto.UserDto;
import ru.upmt.webServerBot.web.mappers.NotificationMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log
@Service
public class NotificationDtoService {
    private final NotificationsRepository notificationsRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    public NotificationDtoService(NotificationsRepository notificationsRepository, NotificationMapper notificationMapper, UserRepository userRepository) {
        this.notificationsRepository = notificationsRepository;
        this.notificationMapper = notificationMapper;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<NotificationDto> findNotificationsByChatId(long chatId) {
        List<Notification> notifications = notificationsRepository.findByChatId(chatId);
        return notificationMapper.toDto(notifications);
    }

    public Optional<NotificationDto> findNotificationById(long id) {
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

    public NotificationDto getUserDto(long id) {
        NotificationDto notificationDto = null;
        List<UserDto> userDtos = new ArrayList<>();
        List<Map<String, String>> listNames = userRepository.findUsersByNotificationId(id);
        listNames = listNames.stream()
//                .filter(row -> row.get("firstName") != null && row.get("lastName") != null)
                .map(row -> {
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("firstName", row.values().stream().filter(value -> !value.isEmpty()).findFirst().get());
                    userMap.put("lastName", row.values().stream().filter(value -> !value.endsWith(".")).findFirst().get());
                    return userMap;
                })
                .toList();
        log.info("listNames: " + listNames.stream().toList());
        for (Map<String, String> map : listNames) {
            userDtos.add(new UserDto(map.get("firstName"), map.get("lastName")));
            log.info("userDto: " + map.get("firstName") + " " + map.get("lastName"));
        }
        Optional<Notification> notification = notificationsRepository.findById(id);
        if (notification.isPresent()) {
            notificationDto = notificationMapper.toDto(notification.get());
            log.info("notificationDto: " + notificationDto.getComment());

            notificationDto.setUsers(userDtos);
            log.info("userDto of notificationDto: " + notificationDto.getUsers());
        }
        return notificationDto;
    }
}
