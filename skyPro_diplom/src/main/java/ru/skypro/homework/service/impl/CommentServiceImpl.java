package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.CommentsMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentsMapper commentsMapper;
    private final EntityManager entityManager;

    /**
     * Получение коментраия по id объявления
     */
    @Override
    public CommentsDto get(Integer id) {
        log.info("Вы вызвали метод получения всех коменнтариев");
        List<CommentEntity> comments = commentRepository.findAllByAdId(id);
        CommentsDto commentsDto = new CommentsDto();
        commentsDto.setCount(comments.size());
        commentsDto.setResults(commentsMapper.commentsToCommentsDTO(comments));
        return commentsDto;
    }

    /**
     * Добавляет комментарий к объявлению
     */
    @Override
    public CommentDto create(Integer id, CreateOrUpdateCommentDto newComment,String userName) {
        log.info("Вызван добавления комментария");
        AdEntity ad = adRepository.getReferenceById(id);
        UserEntity user = userRepository.findByEmail(userName).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("User not found");
        });
        CommentEntity comment = new CommentEntity();
        comment.setAd(ad);
        comment.setText(newComment.getText());
        comment.setAuthor(user);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
        ad.setComment(comment);
        adRepository.save(ad);
        log.info("Вы успешно изменили пароль");
        return commentsMapper.commentToCommentDTO(comment);
    }

    /**
     * Удаляет комментарий к объявлению
     */
    @Override
    @Transactional
    public void delete(Integer adId, Integer commentId) {
        log.info("Вы вызвали метод удаления комментария");
        AdEntity ad = adRepository.getReferenceById(adId);

        entityManager.createNativeQuery("DELETE FROM ads_comments WHERE comments_id = ?")
                .setParameter(1, commentId)
                .executeUpdate();

        adRepository.save(ad);
        log.info("Вы успешно удалили комментарий");
        commentRepository.deleteById(commentId);
    }

    /**
     * Редактирование комментария.
     */
    @Override
    @Transactional
    public CommentDto update(Integer adId, Integer commentId, CreateOrUpdateCommentDto newComment) {
        log.info("Вы вызвали метод обновления комментария");
        AdEntity ad = adRepository.findById(adId).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new AdNotFoundException("Ad not found");
        });

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.info("Комментарий не найден", CommentNotFoundException.class);
            return new CommentNotFoundException("Comment not found");
        } );
        comment.setText(newComment.getText());

        entityManager.createNativeQuery("DELETE FROM ads_comments WHERE comments_id = ?")
                .setParameter(1, commentId)
                .executeUpdate();

        commentRepository.save(comment);
        ad.setComment(comment);
        adRepository.save(ad);
        log.info("Комментарий удалён");
        return commentsMapper.commentToCommentDTO(comment);
    }
}
