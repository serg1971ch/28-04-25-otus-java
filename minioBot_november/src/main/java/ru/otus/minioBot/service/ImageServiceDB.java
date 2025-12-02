package ru.otus.minioBot.service;

import jakarta.transaction.Transactional;
import ru.otus.minioBot.model.ImageTask;

import java.util.List;

public interface ImageServiceDB {

//    List<byte[]> getImageTask(Long notificationId);

    @Transactional
    List<ImageTask> getImageTasks(Long notificationId);

    void deleteImageTaskFromDB(long notificationId);
}
