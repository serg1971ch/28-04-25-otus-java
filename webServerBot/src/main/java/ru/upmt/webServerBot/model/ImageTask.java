package ru.upmt.webServerBot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Access(AccessType.FIELD)
@Table(name = "notes_images")
public class ImageTask {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

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
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "bytes", columnDefinition = "bytea")
    private byte[] bytes;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    public ImageTask(UUID id, byte[] bytes, String mediaType, Notification notification, String name, String pathFile, long size) {
        this.id = id;
        this.name = name;
        this.pathFile = pathFile;
        this.mediaType = mediaType;
        this.size = size;
        this.bytes = bytes;
        this.notification = notification;
    }

    @Builder
    public ImageTask(byte[] bytes, String mediaType, String name, Notification notification, String pathFile,
                     long size) {
        this.name = name;
        this.pathFile = pathFile;
        this.mediaType = mediaType;
        this.size = size;
        this.bytes = bytes;
        this.notification = notification;
    }

    public ImageTask() {

    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}

