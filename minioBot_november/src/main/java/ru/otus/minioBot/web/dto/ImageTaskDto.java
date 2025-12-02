package ru.otus.minioBot.web.dto;

import lombok.Data;

@Data
public class ImageTaskDto {
    private Long id;
    private String name;
    private String pathFile;
    private String mediaType;
    private long size;
    private byte[] data;
    private String base64Image;
}
