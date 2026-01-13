package ru.upmt.webServerBot.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.upmt.webServerBot.model.ExecuteNotification;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.repository.ExecuterRepository;
import ru.upmt.webServerBot.service.NotificationService;
import ru.upmt.webServerBot.service.UserService;
import ru.upmt.webServerBot.service.ExecuteNotificationService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExecuteNotificationServiceImpl implements ExecuteNotificationService {

    private final UserService userService;
    private final ExecuterRepository repository;

    public ExecuteNotificationServiceImpl(UserService userService,  ExecuterRepository repository) {
        this.userService = userService;
        this.repository = repository;
    }

    @Transactional
    @Override
    public ExecuteNotification saveNotification(String nameUser, Notification notification) {

        User user = userService.getUser(nameUser);
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
//        users.add(user);
        ExecuteNotification executeNotification = new ExecuteNotification();
        executeNotification.setNotification(notification);
        executeNotification.setUser(user);
        repository.save(executeNotification);
        return executeNotification;
    }

    @Override
    public User getUser(long id) {
        return repository.getUserByNotificationId(id);
    }

    @Override
    public List<User> getUsers(long notificationid) {
        return repository.findByNotification(notificationid);
    }
}
