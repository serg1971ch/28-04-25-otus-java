package ru.skypro.homework.auth;


import org.assertj.core.api.Assertions;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.dto.RoleDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MyUserDetailsServiceTest {

    @InjectMocks
    private MyUserDetailsService userDetailsService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testCreateUser() {
        UserEntity user = new UserEntity();
        user.setEmail("user@email.com");
        user.setPassword("password");
        user.setRole(RoleDto.USER);
        UserDetails userDetails = new MyUserPrincipal(user);

        userDetailsService.createUser(userDetails);

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    public void testLoadUserByUsernamePositive() {
        String email = "user@example.com";
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword("password");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        Assertions.assertThat(userDetails).isNotNull();
        Assertions.assertThat(userDetails.getUsername()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testLoadUserByUsernameNegative() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });
        verify(userRepository, times(1)).findByEmail(email);
    }


    @Test
    public void testChangePassword_Failure() {
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedPassword = "encodedNewPassword";
        String userEmail = "test@example.com";

        UserEntity user = new UserEntity();
        user.setEmail(userEmail);
        user.setPassword(oldPassword);
        user.setRole(RoleDto.USER);

        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(encoder.encode(newPassword)).thenReturn(encodedPassword);

        userDetailsService.changePassword(oldPassword, newPassword);

        Assertions.assertThat(user.getPassword()).isEqualTo(encodedPassword);
        verify(userRepository, times(1)).save(user);
    }
}