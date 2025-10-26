package ru.shiba.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shiba.model.Notification;

import java.time.LocalDateTime;
import java.util.Collection;

public interface NotificationsRepository extends JpaRepository<Notification, Integer> {
    Collection<Notification> findByNotificationDate(LocalDateTime now);
}
