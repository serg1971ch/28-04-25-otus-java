package ru.otus.minioBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.minioBot.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByChatId(long chatId);
    Long findNotificationById(Long id);
    List<Notification> findByChatIdAndSentDateAfter(Long chatId, LocalDateTime dateTime);
}
