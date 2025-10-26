package ru.skypro.homework.exceptions;

public class CommentNotFoundException extends RuntimeException{
    public CommentNotFoundException(String str)  {
        super(str);
    }
}
