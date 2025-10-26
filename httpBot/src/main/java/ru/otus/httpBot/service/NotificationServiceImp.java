package ru.otus.httpBot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.httpBot.CommandConst;
import ru.otus.httpBot.exceptions.EmptyMessageExceprion;
import ru.otus.httpBot.exceptions.IncorrectMessageException;
import ru.otus.httpBot.model.Notification;
import ru.otus.httpBot.repository.NotificationsRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationServiceImp implements NotificationService {
    //    private static final String REGEX_MSG = "(\\d{2}:\\d{2})";
    private static final String REGEX_MSG = "^.*";
    private final MessageParsingService messageParsingService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImp.class);
    private final NotificationsRepository repository;
    private final TelegramBot bot;

    public NotificationServiceImp(MessageParsingService messageParsingService, NotificationsRepository repository, TelegramBot bot) {
        this.messageParsingService = messageParsingService;
        this.repository = repository;
        this.bot = bot;
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

    //    @Override
//    public Notification parseMessage(String text, int chatId) throws IncorrectMessageException {
//        Notification notification = messageParsingService.parseRemarkMessage(text, chatId);;
//        repository.save(notification);
//        String message = "данные успешно сохранены";
//        sendMessage(chatId,  message);
//        logger.info("message was sent: {}", message);
//        return notification;
//    }
    public void parseMessage(int chatId, String message) {
        Pattern TASK_PATTERN = Pattern.compile(REGEX_MSG);
        if (message == null || message.trim().isEmpty()) {
            throw new EmptyMessageExceprion("Сообщение не может быть пустым.");
        }
            Matcher matcher = TASK_PATTERN.matcher(message);

            if (matcher.matches()) {
                sendMessage(chatId, CommandConst.HELP_MSG_TITLE);
            }
        }

    @Override
    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        bot.execute(sendMessage);
        logger.info("message was sent: {}", message);
    }

//    private String extractPotentialDateTime(String text) {
    // Здесь должна быть ваша логика для извлечения части текста,
    // которая потенциально является датой/временем.
    // Например, если сообщение "напомни мне 13.10.2025 15:30 пойти"
    // Вы можете использовать регулярные выражения.

    // Пример: поиск паттерна "DD.MM.YYYY HH:MM"
//        Pattern pattern = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}");
//        Matcher matcher = pattern.matcher(text);
//        if (matcher.find()) {
//            return matcher.group(0); // Возвращаем найденную дату/время
//        }
//
//        return text;
//    }
//        Notification notification = null;
//
//        Pattern pattern = Pattern.compile(REGEX_MSG);
//        Matcher matcher = pattern.matcher(message);
//
//        if (matcher.find()) {
//            String messageToSave = matcher.group(3);
//            LocalDateTime messageLocalDateTime = LocalDateTime.parse(messageToSave, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
//            if (messageLocalDateTime.isAfter(LocalDateTime.now())) {
//                notification = new Notification(messageToSave, messageLocalDateTime);
//                logger.info("Saving {} to db", notification);
//                repository.save(notification);
//            } else {
//                logger.info("Not saving {} to db", notification);
//                throw new IncorrectMessageException("Not saving message");
//            }
//        }
//        return Optional.ofNullable(notification);
//    }


    @Override
    public void sendMessage(Notification notification) {
        sendMessage(notification.getChatId(), notification.getTitle());
    }
}
