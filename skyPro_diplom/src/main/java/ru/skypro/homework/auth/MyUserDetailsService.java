package ru.skypro.homework.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.dto.RoleDto;

@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsManager {
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    public MyUserDetailsService(PasswordEncoder encoder, UserRepository userRepository) {
        this.encoder = encoder;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.info("Имя пользователя не найдено: " + email);
            return new UserNotFoundException("User not found " + email);
        });
        return new MyUserPrincipal(user);
    }

    @Override
    public void createUser(UserDetails user) {
        UserEntity user1 = new UserEntity();
        user1.setPassword(user.getPassword());
        user1.setEmail(user.getUsername());
        user1.setRole(RoleDto.valueOf(
                user.getAuthorities()
                        .stream()
                        .findFirst()
                        .orElseThrow()
                        .getAuthority()
                        .replace("ROLE_", "")));
        userRepository.save(user1);
        log.info("Добавлен новый пользоваетель: " + user1.getEmail());
    }

    @Override
    public void updateUser(UserDetails user) {
        UserEntity userEdit = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("user not found " + user.getUsername());
        });
        userEdit.setEmail(user.getUsername());
        userRepository.save(userEdit);
        log.info("Пользователь обновлен");
    }

    @Override
    public void deleteUser(String username) {
        UserEntity userToDelete = userRepository.findByEmail(username).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("user not found " + username);
        });
        userRepository.delete(userToDelete);
        log.info("Пользователь удален " + userToDelete);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTH " + auth.getName());
        UserEntity user = userRepository.findByEmail(auth.getName()).orElseThrow(() -> {

            log.info("User not found", UserNotFoundException.class);
            return new UserNotFoundException("User not found " + auth.getName());
        });

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .passwordEncoder(this.encoder::encode)
                        .password(newPassword)
                        .username(user.getEmail())
                        .roles(user.getRole().name())
                        .build();

        user.setPassword(userDetails.getPassword());
        userRepository.save(user);
        log.info("Пароль изменен");
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.findByEmail(username).isPresent();
    }
}
