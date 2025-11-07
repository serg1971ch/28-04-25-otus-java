package ru.otus.httpBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.httpBot.model.Notification;

import java.time.LocalDateTime;
import java.util.Collection;

public interface NotificationsRepository extends JpaRepository<Notification, Integer> {
    Collection<Notification> findByNotificationDate(LocalDateTime now);
}
