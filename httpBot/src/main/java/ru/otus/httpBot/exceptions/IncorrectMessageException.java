package ru.otus.httpBot.exceptions;

public class IncorrectMessageException extends RuntimeException {
    public IncorrectMessageException(String message) {
        super(message);
    }
}
