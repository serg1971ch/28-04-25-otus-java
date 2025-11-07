package ru.otus.minioBot.exceptions;

public class ImageNotFoundException extends RuntimeException{
    public ImageNotFoundException(String message) {
        super(message);
    }
}
