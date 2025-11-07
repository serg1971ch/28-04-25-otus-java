package ru.otus.minioBot.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.minioBot.exceptions.ImageUploadException;
import ru.otus.minioBot.exceptions.NotificationNotFoundException;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.repository.NotificationsRepository;
import ru.otus.minioBot.service.ImageServiceMinio;
import ru.otus.minioBot.service.props.MinioProperties;

import java.io.InputStream;

@Log
@Service
@RequiredArgsConstructor
public class ImageServiceMinioImpl implements ImageServiceMinio {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ImageServiceDBImpl imageServiceDB;
    private final NotificationsRepository notificationsRepository;
    private final ImageRepository imageRepository;

    @Override
    public String upload(Long notificationId) {
        try {
            createBucket();
        } catch (Exception e) {
            throw new ImageUploadException("Image upload failed: "
                    + e.getMessage());
        }
        MultipartFile file = (MultipartFile) imageRepository.findByNotification_Id(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Замечание" + notificationId + " не найдено"));
        String originalfileName = notificationsRepository.getById(notificationId).getComment();

        log.info("name fo :" + originalfileName);
        if (file.isEmpty()) {
            throw new ImageUploadException("Image must have name.");
        }

        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (Exception e) {
            throw new ImageUploadException("Image upload failed: "
                    + e.getMessage());
        }
        saveImage(inputStream, originalfileName);
        return originalfileName;
    }

    @SneakyThrows
    private void createBucket() {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
    }

    private Pair<byte[], String> findImageName(Long imageId) {
        return imageServiceDB.getImagesFromDB(imageId);
    }

    @SneakyThrows
    private void saveImage(InputStream inputStream, String fileName) {
        minioClient.putObject(PutObjectArgs.builder()
                .stream(inputStream, inputStream.available(), -1)
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }
}
