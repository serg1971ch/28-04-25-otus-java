package ru.otus.minioBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "notes_images")
public class ImageTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename")
    private String name;

    @Column(name = "path_file")
    private String pathFile;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "size")
    private long size;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "bytes")
    private byte[] bytes;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;
}

