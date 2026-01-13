package ru.upmt.webServerBot.web.dto;

import lombok.Data;
import java.util.UUID;;

@Data
public class ImageTaskDto {
    private UUID id;
    private String name;
    private String pathFile;
    private String mediaType;
    private long size;
    private byte[] data;
    private String base64Image;
}
