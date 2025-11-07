package ru.otus.minioBot.service;

import jakarta.persistence.criteria.Path;
import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;

import java.util.List;

public interface ImageServiceDB {

//    List<byte[]> getImageTask(Long notificationId);

    @Transactional
    List<ImageTask> getImageTasks(Long notificationId);

    void deleteImageTaskFromDB(long notificationId);
}
