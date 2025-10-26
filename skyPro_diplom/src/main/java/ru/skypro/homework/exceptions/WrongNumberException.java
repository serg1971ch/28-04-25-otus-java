package ru.skypro.homework.exceptions;

public class WrongNumberException extends RuntimeException {
    public WrongNumberException(String str)  {
        super(str);
    }
}
