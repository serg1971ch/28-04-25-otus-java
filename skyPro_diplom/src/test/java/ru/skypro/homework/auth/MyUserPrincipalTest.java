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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MyUserPrincipalTest {

    private UserEntity user;
    private MyUserPrincipal userPrincipal;

    @BeforeEach
    public void setUp() {
        user = new UserEntity();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setRole(RoleDto.USER);

        userPrincipal = new MyUserPrincipal(user);
    }

    @Test
    public void testGetUsername() {
        Assertions.assertThat("user@example.com").isEqualTo(userPrincipal.getUsername());
    }

    @Test
    public void testGetPassword() {
        Assertions.assertThat("password").isEqualTo(userPrincipal.getPassword(), "Password should match the user's current password");
    }

    @Test
    public void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        Assertions.assertThat(authorities).hasSize(1);
        Assertions.assertThat(authorities).isNotEmpty();
        Assertions.assertThat(authorities).extracting(GrantedAuthority::getAuthority).contains("USER");
    }
    @Test
    public void testIsAccountNonExpired() {
        Assertions.assertThat(userPrincipal.isAccountNonExpired()).isTrue();
    }

    @Test
    public void testIsAccountNonLocked() {
        Assertions.assertThat(userPrincipal.isAccountNonLocked()).isTrue();
    }

    @Test
    public void testIsCredentialsNonExpired() {
        Assertions.assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    public void testIsEnabled() {
        Assertions.assertThat(userPrincipal.isEnabled()).isTrue();
    }
}