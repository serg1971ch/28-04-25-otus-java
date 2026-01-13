package ru.upmt.webServerBot.service;

import ru.upmt.webServerBot.model.ExecuteNotification;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.User;

import java.util.List;

public interface ExecuteNotificationService {
    ExecuteNotification saveNotification(String nameUser, Notification notification);
    User getUser(long id);

    List<User> getUsers(long notificationId);
}
