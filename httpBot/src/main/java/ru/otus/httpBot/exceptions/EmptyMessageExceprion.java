package ru.otus.httpBot.exceptions;

public class EmptyMessageExceprion extends RuntimeException {
    public EmptyMessageExceprion(String message) {
        super("Empty message: " + message);
    }
}
