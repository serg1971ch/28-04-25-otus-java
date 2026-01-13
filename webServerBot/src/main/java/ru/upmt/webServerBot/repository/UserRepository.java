package ru.upmt.webServerBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.web.dto.NotificationDto;

import java.util.List;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getUserByLastName(String lastName);

    @Query(
          """
       SELECT u.firstName, u.lastName
        FROM User u
        JOIN ExecuteNotification en ON u.id = en.user.id
        WHERE en.notification.id = :notificationId"""
    )
    List<Map<String,String>> findUsersByNotificationId(@Param("notificationId") Long notificationId);
}
