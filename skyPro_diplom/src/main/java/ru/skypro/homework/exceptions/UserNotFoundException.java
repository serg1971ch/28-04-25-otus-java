package ru.skypro.homework.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String str) {
        super(str);
    }

    public UserNotFoundException() {
        super("User not found");
    }
}
