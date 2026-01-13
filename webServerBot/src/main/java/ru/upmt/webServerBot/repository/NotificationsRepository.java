package ru.upmt.webServerBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.upmt.webServerBot.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification,Long> {

    List<Notification> findByChatId(long chatId);
    Long findNotificationById(long id);

    List<Notification> findByChatIdAndSentDateAfter(Long chatId, LocalDateTime dateTime);
//    Notification findByChatIdAndId(Long chatId, long id);

    @Modifying
    @Query(value = "UPDATE notifications SET position = :position WHERE id = :id", nativeQuery = true)
    int updatePositionById(@Param("id") Long id, @Param("position") String position);

}
