package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.control.MappingControl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentsMapper commentsMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CommentServiceImpl commentService;

    private AdEntity adEntity;
    private UserEntity userEntity;
    private CommentEntity commentEntity;
    private CommentDto commentDto;
    private CommentsDto commentsDto;
    private CreateOrUpdateCommentDto createOrUpdateCommentDto;

    @BeforeEach
    public void setUp() {
        adEntity = new AdEntity();
        adEntity.setId(1);

        userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");

        commentEntity = new CommentEntity();
        commentEntity.setId(1);
        commentEntity.setText("Test comment");
        commentEntity.setAuthor(userEntity);
        commentEntity.setCreatedAt(LocalDateTime.now());

        commentDto = new CommentDto();
        commentDto.setText("Test comment");

        commentsDto = new CommentsDto();
        commentsDto.setCount(1);
        commentsDto.setResults(List.of(commentDto));

        createOrUpdateCommentDto = new CreateOrUpdateCommentDto();
        createOrUpdateCommentDto.setText("Updated comment");

    }

    @Test
    public void testGetComments() {
        when(commentRepository.findAllByAdId(1)).thenReturn(List.of(commentEntity));
        when(commentsMapper.commentsToCommentsDTO(List.of(commentEntity))).thenReturn(List.of(commentDto));

        CommentsDto result = commentService.get(1);

        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getText()).isEqualTo("Test comment");
    }

    @Test
    public void testCreateComment_UserNotFound() {
        when(adRepository.getReferenceById(1)).thenReturn(adEntity);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(1, createOrUpdateCommentDto, "test@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    public void testUpdateComment_AdNotFound() {
        when(adRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(1, 1, createOrUpdateCommentDto))
                .isInstanceOf(AdNotFoundException.class)
                .hasMessage("Ad not found");
    }

    @Test
    public void testUpdateComment_CommentNotFound() {
        when(adRepository.findById(1)).thenReturn(Optional.of(adEntity));
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(1, 1, createOrUpdateCommentDto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found");
    }
}