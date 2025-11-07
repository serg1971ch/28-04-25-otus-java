package ru.otus.minioBot.service.impl;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.ImageProcessingException;
import ru.otus.minioBot.exceptions.NotificationNotFoundException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.model.NotificationStatus;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.repository.NotificationsRepository;
import ru.otus.minioBot.service.ImageServiceDB;
import ru.otus.minioBot.service.ImageServiceFS;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@NoArgsConstructor(force = true)
//@PropertySource("classpath:application.yaml")
public class ImageServiceDBImpl implements ImageServiceDB {
    private final ImageRepository imageRepository;

//    private final NotificationsRepository notificationRepository;

    private ImageTask imageTask;
    Logger  logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    public ImageServiceDBImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public List<ImageTask> getTmageTask(Long notificationId) throws NotificationNotFoundException {
        return imageRepository.findBytesAndNameByNotificationId(notificationId);
    }


    @Override
    @Transactional
    public List<ImageTask> getImageTasks(Long notificationId) {
        List<ImageTask> imageTasks = imageRepository.findByNotificationId(notificationId);
        logger.info("Из репозитория ImageRepository возвращаются фотки: {}" + imageTasks.stream().map(ImageTask::getBytes));
        return imageTasks;

    }


    @Override
    public void deleteImageTaskFromDB(long notificationId) {
        assert imageRepository != null;
        Optional<ImageTask> images = Optional.of(imageRepository.findByNotification_Id(notificationId).orElseThrow());
        images.ifPresent(imageRepository::delete);
        }
    }






