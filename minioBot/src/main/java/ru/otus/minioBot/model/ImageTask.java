package ru.otus.minioBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class ImageTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pathFile;
    private long size;
    private String mediaType;
    @Lob
    private byte[] bytes;
    @OneToOne
    @JoinColumn(name = "student_id")
    private Notification notification;
}
