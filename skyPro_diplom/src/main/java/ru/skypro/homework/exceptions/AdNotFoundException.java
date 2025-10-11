package ru.skypro.homework.exceptions;

public class AdNotFoundException extends RuntimeException{

    public AdNotFoundException(String str)  {
        super(str);
    }
}
