package ru.otus.minioBot.repository;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.minioBot.model.ImageTask;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageTask, Long> {
    Optional<ImageTask> findByNotification_Id(Long id);

    @NotNull
    @Override
    Optional<ImageTask> findById(@NotNull Long imageId);

    Long findAllImagersBySizeAndBytes(long size, byte[] bytes);

    void deleteByName(String imageTask);

    String findByName(String name);

    String findAllByBytes(byte[] bytes);

    String findNameByBytes(byte[] bytes);

    @Query("SELECT i FROM ImageTask i WHERE i.notification.id = :noteId")
    List<ImageTask> findByNotificationId(@Param("noteId") Long notificationId);
//
//    byte[] getImageDataByNotificationId(Long notificationId);

    @Transactional
    @Query("SELECT j.bytes FROM ImageTask j WHERE j.notification.id = :notificationId")
    List<ImageTask> findBytesAndNameByNotificationId(@Param("notificationId") Long notificationId);

}
