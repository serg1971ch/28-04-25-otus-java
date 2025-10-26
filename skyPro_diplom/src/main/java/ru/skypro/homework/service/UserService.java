package ru.skypro.homework.service;


import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public interface UserService {

    boolean setNewPassword(NewPasswordDto newPassword);
    UserDto getUser();
    UpdateUserDto updateUser(UpdateUserDto userPatch);
}
