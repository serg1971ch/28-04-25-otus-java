package ru.otus.httpBot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.httpBot.exceptions.EmptyMessageExceprion;
import ru.otus.httpBot.exceptions.IncorrectMessageException;
import ru.otus.httpBot.model.Notification;
import ru.otus.httpBot.model.NotificationStatus;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessageParsingService {
    private final Logger logger = LoggerFactory.getLogger(MessageParsingService.class);
    private NotificationStatus status;

    private static final Pattern TASK_PATTERN = Pattern.compile("^(.*?)(.[поз].\\d{3})+(.*)(.[да|нет])$");

    // Если "s" в конце - это условное обозначение, и нам нужно его убрать:
    // private static final Pattern TASK_PATTERN_WITH_S = Pattern.compile("^(.*?)s? +(\\d+)s? +(.*)s?$");


    public Notification parseRemarkMessage(String userInput, int chatId) {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new IncorrectMessageException("Сообщение не может быть пустым.");
        }

        // Если "s" в конце — это просто пример, и нам нужно обработать строку без него:
        String cleanedInput = userInput.trim();
        // if (cleanedInput.endsWith("s")) {
        //     cleanedInput = cleanedInput.substring(0, cleanedInput.length() - 1).trim();
        // }

        Matcher matcher = TASK_PATTERN.matcher(cleanedInput);

        if (matcher.matches()) {
            String remarkTitle = matcher.group(1).trim();
            String position = matcher.group(2).trim();
            String violationDescription = matcher.group(3).trim();

            // Тут можно добавить дополнительную валидацию, например, что remarkTitle и violationDescription не пустые
            if (remarkTitle.isEmpty() || violationDescription.isEmpty()) {
                throw new EmptyMessageExceprion("Название замечания и описание нарушения не могут быть пустыми.");
            }

            logger.debug("Parsed remark: Title='{}', Position='{}', Description='{}'",
                    remarkTitle, position, violationDescription);

            String remarkCompleted = matcher.group(4).trim();
            if (remarkCompleted.isEmpty()) {
                throw new EmptyMessageExceprion("Поле не должно быть пустым");
            } else if (remarkCompleted.equals("да")) {
                this.status = NotificationStatus.COMPLETED;
            } else {
                this.status = NotificationStatus.UNCOMPLETED;
            }
            return new Notification(chatId, remarkTitle, position, violationDescription, status, LocalDateTime.now());

        } else {
            logger.error("Failed to parse remark message. Input: '{}'", userInput);
            // Если нужно, здесь можно добавить более конкретные сообщения об ошибке,
            // например, если не найден приоритет или время.
            throw new IncorrectMessageException("Неверный формат сообщения. Ожидается: 'Название замечания Позиция_установки Описание нарушения'");
        }
    }
}