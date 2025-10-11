package ru.skypro.homework.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.auth.MyUserDetailsService;
import ru.skypro.homework.check.CheckService;
import ru.skypro.homework.config.WebSecurityConfig;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.entity.AvatarEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.AvatarRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AvatarService;
import ru.skypro.homework.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import static java.nio.file.Paths.get;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MyUserDetailsService myUserDetailsService;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           MyUserDetailsService myUserDetailsService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.myUserDetailsService = myUserDetailsService;
    }

    /**
     * Изменение пароля у пользователя
     */
    @Override
    public boolean setNewPassword(NewPasswordDto newPassword) {
        log.info("The setNewPassword method of setNewPassword is called");
        myUserDetailsService.changePassword(newPassword.getCurrentPassword(), newPassword.getNewPassword());
        return true;
    }

    /**
     * Информация о пользователе
     */
    @Override
    public UserDto getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = ((UserDetails) principal).getUsername();
        return userMapper.toUserDto((userRepository.findByEmail(userName).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("User not found");
        })));
    }

    /**
     * Изменение данных пользователя
     */
    @Override
    public UpdateUserDto updateUser(UpdateUserDto userPatch) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = ((UserDetails) principal).getUsername();
        UserEntity user = userRepository.findByEmail(userName).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("User not found");
        });
        user.setFirstName(userPatch.getFirstName());
        user.setLastName(userPatch.getLastName());
        user.setPhone(userPatch.getPhone());
        userRepository.save(user);
        return userPatch;
    }
}
