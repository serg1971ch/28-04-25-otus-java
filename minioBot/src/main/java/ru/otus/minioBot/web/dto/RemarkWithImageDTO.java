package ru.otus.minioBot.web.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;
@Data
public class RemarkWithImageDTO {
    private String comment;
    private List<byte[]> imageData;

    public RemarkWithImageDTO(String comment, List<byte[]> imageData) {
        this.comment = comment;
        this.imageData = imageData;
    }
}

