package ru.otus.minioBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Entity
@Table(name = "notes_images")
public class ImageTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename")
    private String filename;

    @Column(name = "path_file")
    private String pathFile;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "size")
    private long size;

    @Lob
    private byte[] bytes;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Notification notification;
}

