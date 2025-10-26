package ru.skypro.homework.service;



import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AvatarService {
void updateImage(MultipartFile animalPhoto) throws IOException;
    ResponseEntity<byte[]> getAvatar(Long id) throws IOException;
}