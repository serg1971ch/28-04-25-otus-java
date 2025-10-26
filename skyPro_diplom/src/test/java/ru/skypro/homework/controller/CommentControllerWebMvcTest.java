package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.CommentServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest
class CommentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private UserRepository userRepository;

    @SpyBean
    private CommentServiceImpl commentsService;

    @InjectMocks
    private CommentController controller;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void getAllTest() throws Exception {
        Long id = 1L;
        String username = "test@email.com";
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .build();

        CommentEntity comment1 = new CommentEntity();
        comment1.setId(1);
        comment1.setAuthor(user);
        comment1.setText("text 1");
        comment1.setAd(adEntity);

        CommentEntity comment2 = new CommentEntity();
        comment2.setId(1);
        comment2.setAuthor(user);
        comment2.setText("text 2");
        comment2.setAd(adEntity);

        ArrayList<CommentEntity> comments = new ArrayList<>(List.of(comment1, comment2));
        when(commentRepository.findAllByAdId(anyInt())).thenReturn(comments);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/ads/{id}/comments", id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].text")
                        .value("text 1"));
    }

    @Test
    @WithMockUser
    void delCommentTest() throws Exception {
        Integer adId = 1;
        Integer commentId = 1;
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);
        String username = "test@email.com";


        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .build();

        CommentEntity comment = new CommentEntity();
        comment.setId(1);
        comment.setAuthor(user);
        comment.setText("test text");
        comment.setAd(adEntity);
        when(commentRepository.findById(any(Integer.class))).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.delete("/ads/{adId}/comments/{commentId}", adId, commentId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}