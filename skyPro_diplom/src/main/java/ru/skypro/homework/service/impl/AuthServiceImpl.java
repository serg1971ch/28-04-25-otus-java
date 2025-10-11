package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.check.CheckService;
import ru.skypro.homework.dto.LoginDto;
import ru.skypro.homework.dto.RegisterDto;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.mapper.*;

import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final CheckService checkService;

    public AuthServiceImpl(
            PasswordEncoder passwordEncoder,
            UserDetailsManager manager,
            UserRepository userRepository,
            CheckService checkService,
            UserMapper userMapper) {
        this.manager = manager;
        this.encoder = passwordEncoder;
        this.userRepository = userRepository;
        this.checkService = checkService;
        this.userMapper = userMapper;
    }

    @Override
    public boolean login(LoginDto loginDto) {
        log.info("The login method of AuthServiceImpl is called");

        if (!manager.userExists(loginDto.getUsername())) { // Проверяет, существует ли пользователь с указанным именем.
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(loginDto.getUsername()); // Получает информацию о пользователе по имени.
        return encoder.matches(loginDto.getPassword(), userDetails.getPassword()); // Сравнивает введенный пароль с хешированным паролем из базы данных.
    }

    @Override
    public boolean register(RegisterDto register) {
        log.info("The register method of AuthServiceImpl is called");

        Optional<UserEntity> user = userRepository.findByEmail(register.getUsername());
        if (user.isPresent()) {
            return false;
        }
        checkService.checkPhone(register.getPhone());
        UserEntity newUser = userMapper.toUser(register);
        newUser.setPassword(encoder.encode(register.getPassword()));
        userRepository.save(newUser);
        return true;
    }
}
