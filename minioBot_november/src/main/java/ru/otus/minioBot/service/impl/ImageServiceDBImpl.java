package ru.otus.minioBot.service.impl;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.NotificationNotFoundException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.service.ImageServiceDB;

import java.util.List;
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






