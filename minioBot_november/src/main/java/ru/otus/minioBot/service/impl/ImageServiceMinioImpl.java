package ru.otus.minioBot.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.otus.minioBot.exceptions.ImageUploadException;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.service.ImageServiceMinio;
import ru.otus.minioBot.service.props.MinioProperties;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log
@Service
@RequiredArgsConstructor
public class ImageServiceMinioImpl implements ImageServiceMinio {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ImageServiceDBImpl imageServiceDB;

    @Override
    public List<String> upload(Long notificationId) {
        String namePhoto;
        List<String> namePhotos = new ArrayList<>();

        try {
            createBucket();
        } catch (Exception e) {
            throw new ImageUploadException("Image upload failed: " + e.getMessage());
        }

        List<ImageTask> tasks = imageServiceDB.getImageTasks(notificationId);

        for (ImageTask task : tasks) {
            namePhoto = task.getName();

            if (namePhoto.isEmpty()) {
                log.info("No filename found for image data of notificationId: {}" + notificationId);
                throw new ImageUploadException("Image must have name.");
            }

            byte[] data = task.getBytes();

            if (data == null || data.length == 0) {
                log.info("Skipping empty image for filename: {}" + namePhoto);
                continue;
            }
            saveImage(data, namePhoto);
            namePhotos.add(namePhoto);
        }

        return namePhotos;
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

    @SneakyThrows
    private void saveImage(byte[] inputStream, String fileName) {
        minioClient.putObject(PutObjectArgs.builder()
                .stream(new ByteArrayInputStream(inputStream), inputStream.length, -1)
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }

}
