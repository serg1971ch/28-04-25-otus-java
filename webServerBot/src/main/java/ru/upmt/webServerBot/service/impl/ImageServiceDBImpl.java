package ru.upmt.webServerBot.service.impl;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.upmt.webServerBot.exceptions.ImageNotFoundException;
import ru.upmt.webServerBot.exceptions.ImageUploadException;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.model.NotificationStatus;
import ru.upmt.webServerBot.model.TaskComplete;
import ru.upmt.webServerBot.repository.ImageRepository;
import ru.upmt.webServerBot.repository.NotificationsRepository;
import ru.upmt.webServerBot.service.ImageServiceDB;


import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

@Slf4j
@Service
@NoArgsConstructor(force = true)

public class ImageServiceDBImpl implements ImageServiceDB {
    private final ImageRepository imageRepository;
    private final NotificationsRepository notificationsRepository;
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    public ImageServiceDBImpl(ImageRepository imageRepository, NotificationsRepository notificationsRepository) {
        this.imageRepository = imageRepository;
        this.notificationsRepository = notificationsRepository;
    }


    @Override
    public List<ImageTask> getImageTasks(Long notificationId) {
        return imageRepository.findBytesAndNameByNotificationId(notificationId);
    }

    @Override
    @Transactional
    public List<ImageTask> getImageTasks(long notificationId) {
        List<ImageTask> imageTasks = imageRepository.findByNotificationId(notificationId);
        logger.info("Из репозитория ImageRepository возвращаются фотки: {}" + imageTasks.stream().map(ImageTask::getBytes));
        return imageTasks;
    }

    @Transactional
    @Override
    public void saveImageTask(Optional<ImageTask> imageTask) {
        if (imageRepository != null && imageTask.isPresent()) {
            imageRepository.saveAndFlush(imageTask.get());
        } else {
            logger.info("imageTask is null");
        }
    }

    @Override
    public void deleteImageTaskFromDB(long notificationId) {
        assert imageRepository != null;
        Optional<ImageTask> images = Optional.of(imageRepository.findByNotification_Id(notificationId).orElseThrow());
        images.ifPresent(imageRepository::delete);
    }

    @Transactional
    @Override
    public ImageTask saveImageFromUrl(URL imageUrl, Long notificationId) {
        String mediaType = "";
        long mediaSize;
        logger.info("Начинается скачивание изображения из URL: {} для уведомления ID: {}" + imageUrl + notificationId);

        // 1. Скачиваем изображение из URL
        byte[] imageData;
        try (InputStream in = imageUrl.openStream()) {
            imageData = in.readAllBytes(); // Java 9+ метод для чтения всех байтов
            mediaSize = imageData.length;
            logger.info("Изображение успешно скачано. Размер: {} байт" + imageData.length);
        } catch (IOException e) {
            logger.info("Ошибка при скачивании изображения из URL: {}" + imageUrl);
            throw new ImageUploadException("Не удалось скачать изображение из URL: " + imageUrl);
        }

        Notification notification = notificationsRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification с ID " + notificationId + " не найдено."));

        // Определяем имя файла
        String noteRemarkBefore = "_before";
        String noteRemarkAfter = "_after";
        String firstfilename = notification.getComment() + noteRemarkBefore;
        String secondFilename = notification.getComment() + noteRemarkAfter;

        logger.info("Состояние task_complete перед присвоением имени изображения: " + notification.getTaskComplete());

        String finalFilename = (notification.getTaskComplete() == TaskComplete.UNCOMPLETED)
                ? firstfilename
                : secondFilename;

        finalFilename = finalFilename.replaceAll("[;:\\s]+", "_").toLowerCase(Locale.ROOT);
        logger.info("finalFilename перед переименованием: " + finalFilename);

        // Определяем корректный MIME и расширение
        try {
            mediaType = extToMime(String.valueOf(imageUrl));
            String ext = Objects.equals(mediaType, "image/jpeg") ? ".jpg" : ".png";
            finalFilename = finalFilename + ext;

        } catch (ImageNotFoundException e) {
            logger.info("not found image {}" + e.getMessage());
        }

        logger.info("Имя файла изображения было преобразован как: " + finalFilename);

        notification.setTaskComplete(TaskComplete.COMPLETE);
        logger.info("task_complete: " + notification.getTaskComplete());

        ImageTask imageTask = new ImageTask();
//        imageTask = new ImageTask(imageData, mediaType, firstfilename, notification, imageUrl.getPath(), mediaSize);
        imageTask.setBytes(imageData);
        imageTask.setName(finalFilename);
        imageTask.setNotification(notification);
        imageTask.setMediaType(mediaType);
        imageTask.setSize(mediaSize);
        imageTask.setPathFile(imageUrl.getPath());


        log.info(" сохраняем ImageTask() imageUrl: {} \n имя фото: {}\n mediaType: {} \n notification: {} \n ", imageUrl.getPath(), finalFilename, mediaType, notification);
        ImageTask saved = imageRepository.saveAndFlush(imageTask);

        logger.info("Изображение сохранено: id={}, name={}, size={}" + saved.getId() + saved.getName()
                + saved.getSize());
        // 4. Сохраняем ImageTask в базу данных
//        imageRepository.save(imageTask);
//        logger.info("Изображение {} (ID: {}) успешно сохранено в базу данных для уведомления ID: {}" +
//                imageTask.getName() + imageTask.getId() + notificationId);
        return saved;
    }

    @Override
    public int findSizeImages(Long notificationId) {
        return imageRepository.findAllImagesCountByNotificationId(notificationId);
    }

    @Transactional
    public ImageTask saveImageFromUrl(URL imageUrl, long notificationId) {
        String mediaType = "";
        long mediaSize;
        logger.info("Начинается скачивание изображения из URL: {} для уведомления ID: {}" + imageUrl + notificationId);

        // 1. Скачиваем изображение из URL
        byte[] imageData;
        try (InputStream in = imageUrl.openStream()) {
            imageData = in.readAllBytes(); // Java 9+ метод для чтения всех байтов
            mediaSize = imageData.length;
            logger.info("Изображение успешно скачано. Размер: {} байт" + imageData.length);
        } catch (IOException e) {
            logger.info("Ошибка при скачивании изображения из URL: {}" + imageUrl);
            throw new ImageUploadException("Не удалось скачать изображение из URL: " + imageUrl);
        }

        Notification notification = notificationsRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification с ID " + notificationId + " не найдено."));

        // Определяем имя файла
        String noteRemarkBefore = "_before";
        String noteRemarkAfter = "_after";
        String firstfilename = notification.getComment() + noteRemarkBefore;
        String secondFilename = notification.getComment() + noteRemarkAfter;
        log.info("taskCompleted: {}", notification.getTaskComplete());
        String finalFilename = (notification.getTaskComplete() == TaskComplete.UNCOMPLETED)
                ? firstfilename
                : secondFilename;
        finalFilename = finalFilename.replaceAll("[;:\\s]+", "_").toLowerCase(Locale.ROOT);

        if (notification.getTaskComplete() == TaskComplete.UNCOMPLETED) {
            notification.setTaskComplete(TaskComplete.COMPLETE);
        }

        // Определяем корректный MIME и расширение
        try {
            mediaType = extToMime(String.valueOf(imageUrl));
            String ext = Objects.equals(mediaType, "image/jpeg") ? ".jpg" : ".png";
            finalFilename = finalFilename + ext;

        } catch (ImageNotFoundException e) {
            logger.info("not found image {}" + e.getMessage());
        }

        logger.info("Имя файла изображения было преобразован как: {}" + finalFilename);
        ImageTask imageTask = new ImageTask();
//        imageTask = new ImageTask(imageData, mediaType, firstfilename, notification, imageUrl.getPath(), mediaSize);
        imageTask.setBytes(imageData);
        imageTask.setName(finalFilename);
        imageTask.setNotification(notification);
        imageTask.setMediaType(mediaType);
        imageTask.setSize(mediaSize);
        imageTask.setPathFile(imageUrl.getPath());

        log.info(" сохраняем ImageTask() imageUrl: {} \n имя фото: {}\n mediaType: {} \n notification: {} \n ", imageUrl.getPath(), finalFilename, mediaType, notification);
        ImageTask saved = imageRepository.saveAndFlush(imageTask);

        logger.info("Изображение сохранено: id={}, name={}, size={}" + saved.getId() + saved.getName()
                + saved.getSize());
        // 4. Сохраняем ImageTask в базу данных
//        imageRepository.save(imageTask);
//        logger.info("Изображение {} (ID: {}) успешно сохранено в базу данных для уведомления ID: {}" +
//                imageTask.getName() + imageTask.getId() + notificationId);
        return saved;
    }

    @Override
    public int findImageTasksByNotificationId(Long notificationId) {
        return imageRepository.findAllImagesCountByNotificationId(notificationId);
    }

    public static String detectMediaType(Path path, byte[] bytes) throws IOException {
        // 1) probeContentType (OS dependent, по расширению)
        try {
            String mime = Files.probeContentType(path);
            if (mime != null) return mime;
        } catch (Exception ignored) {
        }

        // 2) по имени файла
        String mime = URLConnection.guessContentTypeFromName(path.toString());
        if (mime != null) return mime;

        // 3) по потоку (попробует посмотреть сигнатуру в первых байтах)
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            mime = URLConnection.guessContentTypeFromStream(is);
            if (mime != null) return mime;
        } catch (IOException ignored) {
        }

        // 4) ImageIO: для картинок вернёт формат ("png","jpeg"...)
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                String format = readers.next().getFormatName().toLowerCase();
                switch (format) {
                    case "png":
                        return "image/png";
                    case "jpeg":
                    case "jpg":
                        return "image/jpeg";
                    case "gif":
                        return "image/gif";
                    default:
                        return "image/" + format;
                }
            }
        } catch (IOException ignored) {
        }

        // 5) fallback
        return "application/octet-stream";
    }

    public static String getExtensionWithDot(String path) {
        if (path == null) return "";
        // отбросим query/fragment, если есть
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        int h = path.indexOf('#');
        if (h >= 0) path = path.substring(0, h);

        int lastSlash = path.lastIndexOf('/');
        String filename = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot); // ".jpg"
        }
        return "";
    }

    public static String extToMime(String url) {
        String path = url;
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        int slash = path.lastIndexOf('/');
        String name = (slash >= 0) ? path.substring(slash + 1) : path;
        int dot = name.lastIndexOf('.');
        if (dot < 0) return null;
        String ext = name.substring(dot + 1).toLowerCase();
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}




