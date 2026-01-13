package ru.upmt.webServerBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.upmt.webServerBot.model.ExecuteNotification;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.User;

import java.util.List;

public interface ExecuterRepository extends JpaRepository<ExecuteNotification, Long> {

    @Query("select e.user from ExecuteNotification e where e.notification.id = :notificationId")
    User getUserByNotificationId(@Param("notificationId") Long notificationId);

    @Query("SELECT u FROM User u JOIN ExecuteNotification exec ON u.id = exec.user.id WHERE exec.notification.id = :notificationId")
    List<User> findByNotification(@Param("notificationId") long notificationId);
}
