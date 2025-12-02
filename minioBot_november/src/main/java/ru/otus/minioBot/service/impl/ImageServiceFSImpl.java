package ru.otus.minioBot.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.NotificationNotFoundException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.model.NotificationStatus;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.service.ImageServiceFS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Logger;

@Service
public class ImageServiceFSImpl implements ImageServiceFS {
    Logger logger = Logger.getLogger(ImageServiceFSImpl.class.getName());
    private final ImageRepository imageRepository;
    private  ImageTask imageTask;

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
        ImageTask imageTask = new ImageTask();

        String noteRemarkBefore = "_before";
        String noteRemarkAfter = "_after";
        String firstfilename = notification.getComment() + noteRemarkBefore;
        String secondFilename = notification.getComment() + noteRemarkAfter;

        String finalFilename = (notification.getStatus() == NotificationStatus.UNSENT)
                ? firstfilename
                : secondFilename;
        finalFilename = finalFilename.replaceAll("[;:\\s]+", "_").toLowerCase(Locale.ROOT);

        if (notification.getStatus() == NotificationStatus.UNSENT) {
            notification.setStatus(NotificationStatus.SENT);
        }

        Path path = targetPathFile.getFirst();
        logger.info("That is path: {}" + path);

        // Читаем файл напрямую (без URL!)
        byte[] bytes = Files.readAllBytes(path); // <-- Главное исправление!


        imageTask.setName(finalFilename);
        imageTask.setBytes(bytes);
        imageTask.setNotification(notification);
        imageTask.setPathFile(path.toString());

        logger.info("Saving image: filename={}, size={} bytes" + finalFilename +  bytes.length);

        imageRepository.save(imageTask);
        logger.info("Image saved successfully. Path: {}" + imageTask.getPathFile());

        return imageTask.getName();
    }
}
