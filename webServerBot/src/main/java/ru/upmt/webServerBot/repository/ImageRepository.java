package ru.upmt.webServerBot.repository;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.upmt.webServerBot.model.ImageTask;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageTask, UUID> {
    Optional<ImageTask> findByNotification_Id(long id);

    @NotNull
    @Override
    Optional<ImageTask> findById(@NotNull UUID imageId);

    Long findAllImagersBySizeAndBytes(long size, byte[] bytes);

    @Query(value = "SELECT COUNT(*) FROM notes_images WHERE notification_id = ?1", nativeQuery = true)
    int findAllImagesCountByNotificationId(Long notificationId);


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
