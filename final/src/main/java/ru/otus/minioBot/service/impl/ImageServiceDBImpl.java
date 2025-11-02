package ru.otus.minioBot.service.impl;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public byte[] getImage(Long notificationId) throws NotificationNotFoundException {
        return imageRepository.getImageDataByNotificationId( notificationId);
    }

    public  List<byte[]> getTmageTask(Long notificationId) throws NotificationNotFoundException {
      return imageRepository.findBytesAndNameByNotificationId(notificationId);
    }

    public Pair<byte[], String> getImagesFromDB(long notificationId) {
        logger.info("Was invoked method find avatar from database with student id = {}", notificationId);
        ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Замечание не найдено"));
        return Pair.of(imageTask.getBytes(), imageTask.getName());
    }

    public Pair<byte[], String> getImageFromFS(long notificationId) {
        logger.info("Запущен метод поиска изображения по  id замечания = {}", notificationId);
        try {
            ImageTask imageTask = imageRepository.findByNotification_Id(notificationId)
                    .orElseThrow(() -> new NotificationNotFoundException("Замечание" + notificationId + " не найдено"));
            return Pair.of(Files.readAllBytes(Paths.get(imageTask.getPathFile())), imageTask.getMediaType());
        } catch (IOException e) {
            throw new ImageProcessingException("Фото для замечания: " + notificationId + " не найдено");
        }
    }

    @Override
    public Long saveImage(String filePath, long chatId) {
        logger.info("Was invoked method upload image from file: {}", filePath);
        ImageTask imageTask = new ImageTask();
        try {
            byte[] data = Files.readAllBytes(Paths.get(filePath));
//            String extension = StringUtils.getFilenameExtension(file.getName());
//            Path avatarPath = path.resolve(UUID.randomUUID() + "." + extension);
//            Files.write(avatarPath, data); // Убедитесь, что вы записываете в правильный путь

//            Notification notification = notificationRepository.findById(notificationId)
//                    .orElseThrow(() -> new NotificationNotFoundException("Не найдено замечание: " + notificationId));
//            ImageTask image = imageRepository.findByNotification_Id(notificationId)
//                    .orElseGet(ImageTask::new);

//            image.setNotification(notification);
            imageTask.setBytes(data);
            imageTask.setSize(data.length);
//            image.setMediaType(Files.probeContentType(file.toPath())); // Получаем тип медиа из файла
//            image.setFilename(notification.getComment());
            imageRepository.save(imageTask);
        } catch (IOException e) {
            throw new ImageProcessingException("Фото замечания не сохранено, файл -" + filePath + " is null");
        }
        return imageRepository.findAllImagersBySizeAndBytes(imageTask.getSize(), imageTask.getBytes());
    }

    @Override
    public List<byte[]> getImageTask(Long notificationId) {
        return imageRepository.findBytesAndNameByNotificationId(notificationId);
    }

    @Override
    public void deleteImageTaskFromDB(Pair<byte[], Notification> filePair) {
        Pattern pattern = Pattern.compile("_before");
        assert imageRepository != null;
        String imageFile = imageRepository.findAllByBytes(filePair.getFirst());
        Matcher matcher = pattern.matcher(imageFile);
        if (matcher.find()) {
            imageRepository.deleteByName(imageFile);
        } else{
            logger.info("Невозможно удалить фото.");
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
        String noteRemarkBefore = "_before";
        String noteRemarkAfter = "_after";
        String firstfilename = notification.getComment() + noteRemarkBefore;
        String secondFilename = notification.getComment() +  noteRemarkAfter;

        String finalFilename = (notification.getStatus()).equals(NotificationStatus.UNSENT)
                ? firstfilename
                : secondFilename;
        finalFilename = finalFilename.replaceAll("[;:\\s]+", "_");

        imageTask.setName(finalFilename.toLowerCase(Locale.ROOT));
        imageTask.setSize(targetPathFile.getFirst().length);
        imageTask.setBytes(targetPathFile.getFirst());
        imageTask.setNotification(notification);
        imageTask.setPathFile(path + "/" + firstfilename);

        imageRepository.save(imageTask);
        if(notification.getStatus() == NotificationStatus.UNSENT) {
            notification.setStatus(NotificationStatus.SENT);
        }
        logger.info("папка скачивания: {}", path + "/" + firstfilename);
    }
}



