package ru.otus.minioBot;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


// Новый класс, реализующий MultipartFile
public class PathMultipartFile implements MultipartFile {
    private final Path filePath;
    private final String originalFilename;
    private final String contentType;
    private final String name; // Имя поля формы

    public PathMultipartFile(Path filePath, String originalFilename, String contentType, String name) {
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        try {
            return Files.size(filePath) == 0;
        } catch (IOException e) {
            // Логирование ошибки или выброс исключения
            return true; // Считаем пустым, если не можем определить размер
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.size(filePath);
        } catch (IOException e) {
            // Логирование ошибки или выброс исключения
            return 0;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(filePath);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        Files.copy(filePath, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(filePath);
    }
}
