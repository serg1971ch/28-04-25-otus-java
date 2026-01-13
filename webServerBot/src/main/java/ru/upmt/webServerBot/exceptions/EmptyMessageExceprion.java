package ru.upmt.webServerBot.exceptions;

public class EmptyMessageExceprion extends RuntimeException {
    public EmptyMessageExceprion(String message) {
        super("Empty message: " + message);
    }
}
