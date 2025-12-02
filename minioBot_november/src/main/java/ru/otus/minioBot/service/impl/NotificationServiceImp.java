package ru.otus.minioBot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.IncorrectMessageException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.repository.NotificationsRepository;
import ru.otus.minioBot.service.ImageServiceDB;
import ru.otus.minioBot.service.MessageParsingService;
import ru.otus.minioBot.service.NotificationService;
import ru.otus.minioBot.web.dto.RemarkWithImageDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("notificationService")
public class NotificationServiceImp implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImp.class);

    private final NotificationsRepository repository;
    private final ImageServiceDB imageService;
    private final TelegramBot bot;
    private final MessageParsingService parsing;

    public NotificationServiceImp(NotificationsRepository repository, ImageServiceDB imageService, TelegramBot bot, MessageParsingService parsing) {
        this.repository = repository;
        this.imageService = imageService;
        this.bot = bot;
        this.parsing = parsing;
    }

    @Override
    public void sendNotificationMessage(long chatId) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Collection<Notification> notifications = repository.findByChatId(chatId);
        notifications.forEach(task -> {
            sendMessage(task);
            task.setAsSent();
            logger.info("Notification was sent {} ", task);
        });
        repository.saveAll(notifications);
        logger.info("Notifications saved");
    }

    @Override
    public long scheduleNotification(LocalDateTime notification, long chatId) {
        return 0;
    }

    @Override
    public Optional<LocalDateTime> parseMessage(String text) throws IncorrectMessageException {
        // 1. Извлечь потенциальную дату/время из текста
        String potentialDateTimeString = extractPotentialDateTime(text);

        if (potentialDateTimeString == null || potentialDateTimeString.trim().isEmpty()) {
            return Optional.empty(); // Ничего не нашли, что похоже на дату
        }

        // 2. Определить ожидаемые форматы даты/времени
        // Добавьте все форматы, которые вы ожидаете от пользователя.
        // Пример: "13.10.2025 15:30", "2025-10-13 15:30", "завтра 10:00" (для этого нужна более сложная логика)
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                // Добавьте другие форматы, если ожидаете их от пользователя
        };

        // 3. Попробовать распарсить строку, обрабатывая исключения
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(potentialDateTimeString, formatter);
                return Optional.of(dateTime);
            } catch (DateTimeParseException e) {
                // Продолжаем, пытаемся следующий формат
            }
        }
        // Если ни один формат не подошел
        return Optional.empty();
    }

    @Override
    public Long getIdNotification(Notification notification) {
        return notification.getId();
    }

    @Override
    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        bot.execute(sendMessage);
        logger.info("message was sent: {}", message);
    }

    private String extractPotentialDateTime(String text) {

        // Пример: поиск паттерна "DD.MM.YYYY HH:MM"
        Pattern pattern = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0); // Возвращаем найденную дату/время
        }
        return text;
    }

    @Override
    public void sendMessage(Notification notification) {
        sendMessage(notification.getChatId(), notification.getPosition());
    }

    @Transactional
    @Override
    public Notification processRemarkInput(Long chatId, String commandAndData) throws IncorrectMessageException {
        Notification notification = parsing.parseRemarkMessage(commandAndData, chatId);
        logger.info("notification was taken: {}", notification.getComment());
        repository.save(notification);
        return notification;
    }

    @Override
    @Transactional
    public List<Notification> getNotificationsForChat(long chatId) {
        return repository.findByChatId(chatId);
    }

    @Transactional
    public RemarkWithImageDTO getRemarksForChat(long chatId, long noteId) {
        List<ImageTask> imageTasks = imageService.getImageTasks(noteId);
        String comment = repository.getReferenceById(noteId).getComment();

        // Implement this method in your ImageService
        return new RemarkWithImageDTO(comment, imageTasks);
    }
}
