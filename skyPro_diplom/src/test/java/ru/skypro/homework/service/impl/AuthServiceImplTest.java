package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import ru.skypro.homework.check.CheckService;
import ru.skypro.homework.dto.LoginDto;
import ru.skypro.homework.dto.RegisterDto;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    @Mock
    private UserDetailsManager manager;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CheckService checkService;

    @Mock
    private UserMapper userMapper;


    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogin_UserDoesNotExist() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("nonexistentUser");
        loginDto.setPassword("password");
        when(manager.userExists(loginDto.getUsername())).thenReturn(false);

        boolean result = authService.login(loginDto);

        assertThat(result).isFalse();
        verify(manager, times(1)).userExists(loginDto.getUsername());
        verify(manager, never()).loadUserByUsername(anyString());
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testLogin_UserExistsButPasswordDoesNotMatch() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("existingUser");
        loginDto.setPassword("wrongPassword");
        when(manager.userExists(loginDto.getUsername())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getPassword()).thenReturn("hashedPassword");
        when(manager.loadUserByUsername(loginDto.getUsername())).thenReturn(userDetails);

        when(encoder.matches(loginDto.getPassword(), userDetails.getPassword())).thenReturn(false);

        boolean result = authService.login(loginDto);

        assertThat(result).isFalse();
        verify(manager, times(1)).userExists(loginDto.getUsername());
        verify(manager, times(1)).loadUserByUsername(loginDto.getUsername());
        verify(encoder, times(1)).matches(loginDto.getPassword(), userDetails.getPassword());
    }

    @Test
    public void testLogin_UserExistsAndPasswordMatches() {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("existingUser");
        loginDto.setPassword("correctPassword");
        when(manager.userExists(loginDto.getUsername())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getPassword()).thenReturn("hashedPassword");
        when(manager.loadUserByUsername(loginDto.getUsername())).thenReturn(userDetails);

        when(encoder.matches(loginDto.getPassword(), userDetails.getPassword())).thenReturn(true);

        boolean result = authService.login(loginDto);

        assertThat(result).isTrue();
        verify(manager, times(1)).userExists(loginDto.getUsername());
        verify(manager, times(1)).loadUserByUsername(loginDto.getUsername());
        verify(encoder, times(1)).matches(loginDto.getPassword(), userDetails.getPassword());
    }

    @Test
    public void testRegister_UserDoesNotExist_ShouldReturnTrue() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("test@example.com");
        registerDto.setPassword("password");
        registerDto.setPhone("1234567890");

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("encodedPassword");

        when(userRepository.findByEmail(registerDto.getUsername())).thenReturn(Optional.empty());
        when(userMapper.toUser(registerDto)).thenReturn(userEntity);
        when(encoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        boolean result = authService.register(registerDto);

        assertThat(result).isTrue();
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    public void testRegister_UserAlreadyExists_ShouldReturnFalse() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("test@example.com");
        registerDto.setPassword("password");
        registerDto.setPhone("1234567890");
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("encodedPassword");

        when(userRepository.findByEmail(registerDto.getUsername())).thenReturn(Optional.of(userEntity));

        boolean result = authService.register(registerDto);

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any(UserEntity.class));
    }

}