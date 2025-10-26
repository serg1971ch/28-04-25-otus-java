package ru.skypro.homework.service;

import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;


public interface CommentService {
    CommentsDto get(Integer id);
    CommentDto create(Integer id, CreateOrUpdateCommentDto newComment,String name);
    void delete(Integer adId, Integer commentId);
    CommentDto update(Integer adId, Integer commentId, CreateOrUpdateCommentDto newComment);
}
