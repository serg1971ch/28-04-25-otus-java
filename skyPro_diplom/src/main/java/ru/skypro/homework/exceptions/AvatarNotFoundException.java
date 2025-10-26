package ru.skypro.homework.exceptions;

public class AvatarNotFoundException extends RuntimeException{
    public AvatarNotFoundException() {
        super("Avatar not found");
    }
}
