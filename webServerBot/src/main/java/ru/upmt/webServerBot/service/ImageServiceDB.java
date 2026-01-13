package ru.upmt.webServerBot.service;

import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.model.Notification;


import java.nio.file.Path;

import java.util.List;
import java.util.Optional;
import java.net.URL;

public interface ImageServiceDB {

//    List<byte[]> getImageTask(Long notificationId);

    @Transactional
    List<ImageTask> getImageTasks(Long notificationId);

    @Transactional
    List<ImageTask> getImageTasks(long notificationId);

    void saveImageTask(Optional<ImageTask> imageTask);
    int findSizeImages(Long notificationId);

    @Transactional
    void deleteImageTaskFromDB(long notificationId);
    ImageTask saveImageFromUrl(URL imageUrl, Long notificationId);

    int findImageTasksByNotificationId(Long notificationId);
}
