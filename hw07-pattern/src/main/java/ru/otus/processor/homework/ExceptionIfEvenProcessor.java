package ru.otus.processor.homework;

import java.time.LocalDateTime;
import ru.otus.model.Message;
import ru.otus.processor.Processor;

public class ExceptionIfEvenProcessor implements Processor {

    private final DateTimeProvider dateTimeProvider;

    public ExceptionIfEvenProcessor(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public Message process(Message message) {
        LocalDateTime currentDateTime = dateTimeProvider.getCurrentDateTime();
        int currentSecond = currentDateTime.getSecond();

        if (currentSecond % 2 == 0) {
            throw new RuntimeException("Четная секунда! Текущее время: " + currentDateTime);
        }

        // Если секунда нечетная, возвращаем сообщение без изменений
        return message;
    }
}
