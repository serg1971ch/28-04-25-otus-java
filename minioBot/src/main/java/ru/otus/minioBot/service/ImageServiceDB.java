package ru.otus.minioBot.service;

import org.springframework.data.util.Pair;
import ru.otus.minioBot.model.Notification;

public interface ImageServiceDB {
//    void uploadImage(Pair<byte[], String> file, long notificationId);
    void uploadImageFromFStoDB(Pair<byte[], Notification> notificationPair);
    Pair<byte[], String> getImagesFromDB(long notificationId);
    Pair<byte[], String> getImageFromFS(long notificationId);
}
