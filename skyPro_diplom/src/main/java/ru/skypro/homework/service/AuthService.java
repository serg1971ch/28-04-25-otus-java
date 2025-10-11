package ru.skypro.homework.service;

import ru.skypro.homework.dto.LoginDto;
import ru.skypro.homework.dto.RegisterDto;

public interface AuthService {
    boolean login(LoginDto loginDto);
    boolean register(RegisterDto register);
}
