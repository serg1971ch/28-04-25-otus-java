package ru.upmt.webServerBot.service;


import org.springframework.data.util.Pair;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.model.Notification;
;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ImageServiceFS {

    Pair<ImageTask, String> getImagesFromDB(long notificationId);
    Pair<ImageTask, String> getImageFromFS(long notificationId);
    String uploadImageFromFStoDB(Pair<Path, Notification> targetPathFile) throws IOException;
    int findSizeImages(Long notificationId);
}
