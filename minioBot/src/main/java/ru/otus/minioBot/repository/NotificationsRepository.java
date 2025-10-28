package ru.otus.minioBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.minioBot.model.Notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    List<Notification> findBySentDateAfter(LocalDateTime dateTime);
}
