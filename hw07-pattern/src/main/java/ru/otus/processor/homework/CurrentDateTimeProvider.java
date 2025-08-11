package ru.otus.processor.homework;

import java.time.LocalDateTime;

public class CurrentDateTimeProvider implements DateTimeProvider {
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
}
