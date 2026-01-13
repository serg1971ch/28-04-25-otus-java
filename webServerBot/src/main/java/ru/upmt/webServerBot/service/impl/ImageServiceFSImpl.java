package ru.upmt.webServerBot.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.upmt.webServerBot.exceptions.NotificationNotFoundException;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.NotificationStatus;
import ru.upmt.webServerBot.model.TaskComplete;
import ru.upmt.webServerBot.repository.ImageRepository;
import ru.upmt.webServerBot.service.ImageServiceFS;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Logger;

@Service
public class ImageServiceFSImpl implements ImageServiceFS {
    Logger logger = Logger.getLogger(ImageServiceFSImpl.class.getName());
    private final ImageRepository imageRepository;
    private ImageTask imageTask;

    public ImageServiceFSImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;

    }

    public Pair<ImageTask, String> getImagesFromDB(long notificationId) {
        logger.info("Was invoked method find avatar from database with student id = {}" + notificationId);
        ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Замечание не найдено"));
        return Pair.of(imageTask, imageTask.getName());
    }

    public Pair<ImageTask, String> getImageFromFS(long notificationId) {
        logger.info("Запущен метод поиска изображения по  id замечания = {}" + notificationId);
        ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Замечание" + notificationId + " не найдено"));
        return Pair.of(imageTask, imageTask.getMediaType());
    }

    @Transactional
    public String uploadImageFromFStoDB(Pair<Path, Notification> targetPathFile)
            throws NotificationNotFoundException, IOException {

        if (targetPathFile == null) {
            throw new IllegalArgumentException("targetPathFile is null");
        }

        Notification notification = targetPathFile.getSecond();

        String noteRemarkBefore = "_before";
        String noteRemarkAfter = "_after";
        String firstfilename = notification.getComment() + noteRemarkBefore;
        String secondFilename = notification.getComment() + noteRemarkAfter;

        String finalFilename = (notification.getTaskComplete() == TaskComplete.UNCOMPLETED)
                ? firstfilename
                : secondFilename;
        finalFilename = finalFilename.replaceAll("[;:\\s]+", "_").toLowerCase(Locale.ROOT);

        if (notification.getTaskComplete() == TaskComplete.UNCOMPLETED) {
            notification.setTaskComplete(TaskComplete.COMPLETE);
        }

        Path path = targetPathFile.getFirst();
        logger.info("That is path: {}" + path);

        // Читаем файл напрямую (без URL!)
        byte[] bytes = Files.readAllBytes(path); // <-- Главное исправление!


        imageTask.setName(finalFilename);
        imageTask.setBytes(bytes);
        imageTask.setNotification(notification);
        imageTask.setPathFile(path.toString());

        logger.info("Saving image: filename={}, size={} bytes" + finalFilename + bytes.length);

        imageRepository.save(imageTask);
        logger.info("Image saved successfully. Path: {}" + imageTask.getPathFile());

        return imageTask.getName();
    }

    @Override
    public int findSizeImages(Long notificationId) {
        return imageRepository.findAllImagesCountByNotificationId(notificationId);
    }
}
