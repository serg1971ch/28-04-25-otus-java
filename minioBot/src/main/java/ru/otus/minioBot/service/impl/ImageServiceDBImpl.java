package ru.otus.minioBot.service.impl;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.util.Pair;
import ru.otus.minioBot.exceptions.ImageProcessingException;
import ru.otus.minioBot.exceptions.NotificationNotFoundException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.repository.NotificationsRepository;
import ru.otus.minioBot.service.ImageServiceDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@PropertySource("classpath:application.yaml")
public class ImageServiceDBImpl implements ImageServiceDB {

    private final ImageRepository imageRepository;
    private final NotificationsRepository notificationRepository;
    private final Path path;
    private final Logger logger = LoggerFactory.getLogger(ImageServiceDBImpl.class);

    public ImageServiceDBImpl(ImageRepository imageRepository,
                              NotificationsRepository notificationRepository,
                         @Value("photos-dir-name") String applicationDirName) {
        this.imageRepository = imageRepository;
        this.notificationRepository = notificationRepository;
        path = Paths.get(applicationDirName);
    }

    public ImageServiceDBImpl(ImageRepository imageRepository, NotificationsRepository notificationRepository, Path path) {
        this.imageRepository = imageRepository;
        this.notificationRepository = notificationRepository;
        this.path = path;
    }

    @Transactional
    public void uploadImage(MultipartFile multipartFile, long notificationId) {
        logger.info("Was invoked method upload avatar");
        try {
            byte[] data = multipartFile.getBytes();
            String extension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            Path avatarPath = path.resolve(UUID.randomUUID() + "." + extension);
            Files.write(path, data);
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationNotFoundException("Не найдено замечание: " + notificationId));
            ImageTask image = imageRepository.findByNotification_Id(notificationId)
                    .orElseGet(ImageTask::new);
            image.setNotification(notification);
            image.setBytes(data);
            image.setSize(data.length);
            image.setMediaType(multipartFile.getContentType());
            image.setPathFile(avatarPath.toString());
            imageRepository.save(image);
        } catch (IOException e) {
            throw new ImageProcessingException("Фото замечания не сохранено, файл -" + multipartFile.getName() + " is null");
        }
    }

    public Pair<byte[], String> getImagesFromDB(long notificationId) {
        logger.info("Was invoked method find avatar from database with student id = {}", notificationId);
        ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Замечание не найдено"));
        return Pair.of(imageTask.getBytes(), imageTask.getMediaType());
    }

    public Pair<byte[], String> getImageFromFS(long notificationId) {
        logger.info("Was invoked method find avatar from file with student id = {}", notificationId);
        try {
            ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                    .orElseThrow(() -> new NotificationNotFoundException("Замечание" + notificationId + " не найдено"));
            return Pair.of(Files.readAllBytes(Paths.get(imageTask.getPathFile())), imageTask.getMediaType());
        } catch (IOException e) {
            throw new ImageProcessingException("Фото для замечания: " + notificationId + " не найдено");
        }
    }

//    public Page<ImageTask> getAllImages(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        logger.info("Was invoked method for finding all avatars");
//        return imageRepository.findAll(pageable);
//    }

//    public Page<ImageTask> getAllAvatars(Pageable pageable) {
//        return imageRepository.findAll(pageable);
//    }
}
