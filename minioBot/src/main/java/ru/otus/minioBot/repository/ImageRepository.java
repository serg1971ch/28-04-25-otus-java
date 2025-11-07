package ru.otus.minioBot.repository;

import jakarta.persistence.Tuple;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.minioBot.model.ImageTask;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageTask, Long>, PagingAndSortingRepository<ImageTask, Long> {
    Optional<ImageTask> findByNotification_Id(Long id);

    @NotNull
    @Override
    Optional<ImageTask> findById(@NotNull Long imageId);

    Long findAllImagersBySizeAndBytes(long size, byte[] bytes);

    void deleteByName(String imageTask);

    String findByName(String name);

    String findAllByBytes(byte[] bytes);

    byte[] getImageDataByNotificationId(Long notificationId);

    @Query("SELECT i.bytes FROM ImageTask i WHERE i.notification.id = :notificationId")
    List<byte[]> findBytesAndNameByNotificationId(@Param("notificationId") Long notificationId);

}
