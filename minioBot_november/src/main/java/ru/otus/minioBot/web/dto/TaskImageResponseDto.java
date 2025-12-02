package ru.otus.minioBot.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder // Полезно для создания объектов
@Schema(description = "Task Image Response DTO")
public class TaskImageResponseDto {

    private String fileName;
    private String mediaType;
    private long size;
    private String url; // URL для прямого доступа к изображению в Minio

    // Можно добавить ID, если нужно
    private Long id;
}
