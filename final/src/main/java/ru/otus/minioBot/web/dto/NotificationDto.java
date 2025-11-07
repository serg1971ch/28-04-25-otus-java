package ru.otus.minioBot.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.otus.minioBot.model.TaskComplete;
import ru.otus.minioBot.web.validation.OnCreate;
import ru.otus.minioBot.web.validation.OnUpdate;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Task DTO")
public class NotificationDto {
    @NotNull(message = "Id must be not null.",
            groups = OnUpdate.class)
    private Long id;

    @NotNull(message = "Title must be not null.",
            groups = {OnCreate.class, OnUpdate.class})
    private String title;

    private String description;

    private TaskComplete statusComplete;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expirationDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> images;
}
