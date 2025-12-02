package ru.otus.minioBot.web.dto;

import lombok.Data;
import ru.otus.minioBot.model.ImageTask;

import java.util.List;
@Data
public class RemarkWithImageDTO {
    private String comment;
    private List<ImageTask> imageData;

    public RemarkWithImageDTO(String comment, List<ImageTask> imageData) {
        this.comment = comment;
        this.imageData = imageData;
    }
}

