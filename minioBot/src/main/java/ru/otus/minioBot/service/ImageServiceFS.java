package ru.otus.minioBot.service;


import java.nio.file.Path;
import org.springframework.data.util.Pair;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;

import java.io.IOException;
import java.net.MalformedURLException;

public interface ImageServiceFS {

    Pair<ImageTask, String> getImagesFromDB(long notificationId);
    Pair<ImageTask, String> getImageFromFS(long notificationId);
    String uploadImageFromFStoDB(Pair<Path, Notification> targetPathFile) throws IOException;
}
