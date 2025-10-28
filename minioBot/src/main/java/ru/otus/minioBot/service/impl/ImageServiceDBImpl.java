package ru.otus.minioBot.service.impl;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.minioBot.exceptions.ImageProcessingException;
import ru.otus.minioBot.exceptions.NotificationNotFoundException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.model.NotificationStatus;
import ru.otus.minioBot.model.TaskComplete;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.repository.NotificationsRepository;
import ru.otus.minioBot.service.ImageServiceDB;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@NoArgsConstructor(force = true)
@PropertySource("classpath:application.yaml")
public class ImageServiceDBImpl implements ImageServiceDB {
    private final String path;
    private final ImageRepository imageRepository;
    private final NotificationsRepository notificationRepository;

    private final Logger logger = LoggerFactory.getLogger(ImageServiceDBImpl.class);

    @Autowired
    public ImageServiceDBImpl(@Value("${app.download.dir}") String applicationDirName, ImageRepository imageRepository, NotificationsRepository notificationRepository) {
        path = applicationDirName;
        this.imageRepository = imageRepository;
        this.notificationRepository = notificationRepository;
    }

//    public ImageServiceDBImpl(ImageRepository imageRepository, NotificationsRepository notificationRepository, Path path) {
//        this.imageRepository = imageRepository;
//        this.notificationRepository = notificationRepository;
//        this.path = path;
//    }

//    @Transactional
//    public void uploadImage(Pair<byte[], String> file, long notificationId) {
//        logger.info("Was invoked method upload avatar");
//        try {
//            byte[] data = Files.readAllBytes(file.toPath());
//            String extension = StringUtils.getFilenameExtension(file.getName());
//            Path avatarPath = path.resolve(UUID.randomUUID() + "." + extension);
//            Files.write(avatarPath, data); // Убедитесь, что вы записываете в правильный путь
//
//            Notification notification = notificationRepository.findById(notificationId)
//                    .orElseThrow(() -> new NotificationNotFoundException("Не найдено замечание: " + notificationId));
//            ImageTask image = imageRepository.findByNotification_Id(notificationId)
//                    .orElseGet(ImageTask::new);
//            image.setNotification(notification);
//            image.setBytes(data);
//            image.setSize(data.length);
//            image.setMediaType(Files.probeContentType(file.toPath())); // Получаем тип медиа из файла
//            image.setFilename(notification.getComment());
//            imageRepository.save(image);
//        } catch (IOException e) {
//            throw new ImageProcessingException("Фото замечания не сохранено, файл -" + file.getName() + " is null");
//        }
//    }

    public Pair<byte[], String> getImagesFromDB(long notificationId) {
        logger.info("Was invoked method find avatar from database with student id = {}", notificationId);
        ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Замечание не найдено"));
        return Pair.of(imageTask.getBytes(), imageTask.getFilename());
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
    @Transactional
    public void uploadImageFromFStoDB(Pair<byte[], Notification> targetPathFile) throws NotificationNotFoundException {
        if (targetPathFile == null) {
            throw new IllegalArgumentException("One of the parameters is null: targetPathFile=" + targetPathFile);
        }
        assert notificationRepository != null;
        Notification notification = targetPathFile.getSecond();

        // Create ImageTask and set properties
        ImageTask imageTask = new ImageTask();
        String filename = notification.getComment() + TaskComplete.UNCOMPLETED;
        imageTask.setFilename(filename.replace(";","_"));
        imageTask.setSize(targetPathFile.getFirst().length);
        imageTask.setBytes(targetPathFile.getFirst());
        imageTask.setNotification(notification);
        // Save imageTask to the database or perform further processing
        imageRepository.save(imageTask);
        logger.info("папка скачивания: {}", path + "/" + filename);
    }

}



