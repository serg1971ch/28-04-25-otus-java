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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.AdsServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest
class AdsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdRepository adRepository;

    @MockBean
    private UserRepository userRepository;

    @SpyBean
    private AdsServiceImpl adsService;

    private ObjectMapper mapper;

    @InjectMocks
    private AdsController adsController;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void getExtendedAdTest() throws Exception {
        Integer adId = 1;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        AdEntity ad = new AdEntity();
        ad.setId(1);
        ad.setAuthor(userEntity);
        ad.setTitle("Test Ad");
        ad.setDescription("Test Description");

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .email("username")
                .build();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = ((UserDetails) principal).getUsername();
        when(userRepository.findByEmail(userName)).thenReturn(Optional.ofNullable(user));
        when(adRepository.findById(any(Integer.class))).thenReturn(Optional.of(ad));
        mockMvc.perform(MockMvcRequestBuilders.get("/ads/{id}", adId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Ad"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description")
                        .value("Test Description"));
    }

    @Test
    @WithMockUser
    void getAdsDtoTest() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        AdEntity ad1 = new AdEntity();
        ad1.setId(1);
        ad1.setAuthor(userEntity);
        ad1.setTitle("Test Ad 1");
        ad1.setDescription("Test Description 1");

        AdEntity ad2 = new AdEntity();
        ad2.setId(2);
        ad2.setAuthor(userEntity);
        ad2.setTitle("Test Ad 2");
        ad2.setDescription("Test Description 2");

        ArrayList<AdEntity> ads = new ArrayList<>(List.of(ad1, ad2));
        when(adRepository.findAll()).thenReturn(ads);
        mockMvc.perform(MockMvcRequestBuilders.get("/ads"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].title")
                        .value("Test Ad 1"));
    }

    @Test
    @WithMockUser
    void deleteAdTest() throws Exception {
        Integer adId = 1;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .email("username")
                .build();

        AdEntity ad = new AdEntity();
        ad.setId(1);
        ad.setAuthor(userEntity);
        ad.setTitle("Test Ad");
        ad.setDescription("Test Description");
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = ((UserDetails) principal).getUsername();
        when(adRepository.findById(any(Integer.class))).thenReturn(Optional.of(ad));
        when(userRepository.findByEmail(userName)).thenReturn(Optional.ofNullable(user));
        //then
        mockMvc.perform(MockMvcRequestBuilders.delete("/ads/{id}", adId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser
    void updateAd() throws Exception {
        Integer adId = 1;

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .build();

        AdEntity ad = new AdEntity();
        ad.setId(1);
        ad.setAuthor(user);
        ad.setTitle("Test Ad");
        ad.setDescription("Test Description");

        CreateOrUpdateAdDto createAd = new CreateOrUpdateAdDto();
        createAd.setDescription("Test Description");
        createAd.setPrice(100);
        createAd.setTitle("Test Ad");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = ((UserDetails) principal).getUsername();
        when(adRepository.findById(any(Integer.class))).thenReturn(Optional.of(ad));
        when(adRepository.save(any(AdEntity.class))).thenReturn(ad);
        when(userRepository.findByEmail(userName)).thenReturn(Optional.ofNullable(user));
        mockMvc.perform(MockMvcRequestBuilders.patch("/ads/{id}", adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createAd)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Ad"));
    }

    @Test
    @WithMockUser
    void getAdsByUser() throws Exception {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("email@email.ru")
                .phone("+777777")
                .build();

        AdEntity ad1 = new AdEntity();
        ad1.setId(1);
        ad1.setAuthor(user);
        ad1.setTitle("Test Ad 1");
        ad1.setDescription("Test Description 1");

        AdEntity ad2 = new AdEntity();
        ad2.setId(2);
        ad2.setAuthor(user);
        ad2.setTitle("Test Ad 2");
        ad2.setDescription("Test Description 2");

        ArrayList<AdEntity> ads = new ArrayList<>(List.of(ad1, ad2));
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = ((UserDetails) principal).getUsername();
        when(userRepository.findByEmail(userName)).thenReturn(Optional.ofNullable(user));
        when(adRepository.findByAuthorEmail(userName)).thenReturn(ads);
        mockMvc.perform(MockMvcRequestBuilders.get("/ads/me"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].title")
                        .value("Test Ad 1"));
    }

}