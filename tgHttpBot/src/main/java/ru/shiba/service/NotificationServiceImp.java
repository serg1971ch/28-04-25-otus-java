package ru.otus.httpBot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.httpBot.CommandConst;
import ru.otus.httpBot.exceptions.IncorrectMessageException;
import ru.otus.httpBot.model.Notification;
import ru.otus.httpBot.repository.NotificationsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationServiceImp implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImp.class);

    private final NotificationsRepository repository;
    private final TelegramBot bot;
    private final MessageParsingService parsing;

    public NotificationServiceImp(NotificationsRepository repository, TelegramBot bot, MessageParsingService parsing) {
        this.repository = repository;
        this.bot = bot;
        this.parsing = parsing;
    }

    @Override
    public void sendNotificationMessage() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Collection<Notification> notifications = repository.findByNotificationDate(now);
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
    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        bot.execute(sendMessage);
        logger.info("message was sent: {}", message);
    }

    private String extractPotentialDateTime(String text) {
        // Здесь должна быть ваша логика для извлечения части текста,
        // которая потенциально является датой/временем.
        // Например, если сообщение "напомни мне 13.10.2025 15:30 пойти"
        // Вы можете использовать регулярные выражения.

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

    @Override
    public String processRemarkInput(int chatId, String commandAndData) {
        Notification notification = parsing.parseRemarkMessage(commandAndData, chatId);
        sendMessage(notification.getChatId(), CommandConst.HELP_MSG_UNCOMPLETED_PHOTO);
        logger.info("notification was taken: {}", notification.toString());
        return notification.getComment();
    }
}
