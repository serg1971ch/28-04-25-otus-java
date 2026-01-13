package ru.upmt.webServerBot.service;

import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.web.dto.NotificationDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    User getUser(String name);
    void saveUser(User user);

    List<Map<String, String>> getUsers(Long notificationId);
}
