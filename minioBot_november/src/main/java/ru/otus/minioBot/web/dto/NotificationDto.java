package ru.otus.minioBot.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.TaskComplete;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Task DTO")
public class NotificationDto {

    private String comment;

    private TaskComplete statusComplete;

    private String position;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime notificationDate;

    @Getter
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<ImageTaskDto> images;

    public LocalDateTime getSentDate() {
        return notificationDate;
    }
}
