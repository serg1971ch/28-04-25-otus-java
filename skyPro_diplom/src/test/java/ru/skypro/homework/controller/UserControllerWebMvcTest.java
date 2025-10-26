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
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.entity.AvatarEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @SpyBean
    private UserServiceImpl userService;

    @InjectMocks
    private UserController controller;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }


    @Test
    @WithMockUser
    void getUserTest() throws Exception {
        AvatarEntity avatar = new AvatarEntity();
        avatar.setId(1L); // Установите ID аватара

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .email("email@email.ru")
                .avatar(avatar)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        mockMvc.perform(MockMvcRequestBuilders.get("/users/me"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("email@email.ru"));

    }

    @Test
    @WithMockUser
    void updateUserTest() throws Exception {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .email("username")
                .build();

        UpdateUserDto updateUser = new UpdateUserDto();
                updateUser.setFirstName("Test first");
                updateUser.setLastName("test last");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        mockMvc.perform(MockMvcRequestBuilders.patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateUser)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Test first"));
    }


    @Test
    @WithMockUser
    void updateImage() throws Exception {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .email("username")
                .build();
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg",
                "image/jpeg", "test image".getBytes());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/me/image")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}