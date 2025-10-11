package ru.skypro.homework.service.impl;

import org.assertj.core.api.Assertions;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.auth.MyUserDetailsService;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.RoleDto;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.AvatarRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MyUserDetailsService userDetailsService;

    @Mock
    private AvatarRepository avatarRepository;
    @Mock
    private UserDetails userDetails;

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private UserEntity user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("123456789");
        user.setEmail("user@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testChangeToPassword() {
        NewPasswordDto newPasswordDto = new NewPasswordDto();
        newPasswordDto.setCurrentPassword("oldPassword");
        newPasswordDto.setNewPassword("newPassword");

        doNothing().when(userDetailsService).changePassword("oldPassword", "newPassword");

        userService.setNewPassword(newPasswordDto);

        verify(userDetailsService, times(1)).changePassword("oldPassword", "newPassword");
    }

    @Test
    public void testInfoAboutUser() {

        String userEmail = "test@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(userEmail);

        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setEmail(userEmail);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(expectedUserDto);

        UserDto actualUserDto = userService.getUser();

        Assertions.assertThat(actualUserDto).isNotNull();
        Assertions.assertThat(actualUserDto).isEqualTo(expectedUserDto);
    }

    @Test
    public void testGetUserNegative() {
        String userEmail = "test@example.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUser();
        });

        Assertions.assertThat(exception.getMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    public void testUpdateUserPositive() {
        String userEmail = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String phone = "1234567890";

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userEmail);

        UpdateUserDto userPatch = new UpdateUserDto();
        userPatch.setFirstName(firstName);
        userPatch.setLastName(lastName);
        userPatch.setPhone(phone);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(userEntity));

        UpdateUserDto result = userService.updateUser(userPatch);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getFirstName()).isEqualTo(firstName);
        Assertions.assertThat(result.getLastName()).isEqualTo(lastName);
        Assertions.assertThat(result.getPhone()).isEqualTo(phone);

        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    void testUpdateUserNegative() {
        String userEmail = "test@example.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.updateUser(new UpdateUserDto()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }
}