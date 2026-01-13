package ru.upmt.webServerBot.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.upmt.webServerBot.model.TaskComplete;
import ru.upmt.webServerBot.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Task DTO")
public class NotificationDto {

    private long id;

    private String comment;

    private TaskComplete statusComplete;

    private String position;

    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime notificationDate;

    @Getter
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<ImageTaskDto> images;

    public LocalDateTime getSentDate() {
        return notificationDate;
    }

    private List<UserDto> users = new ArrayList<>();

//    public List<Map<String, String>> getUsers() {
//        return users;
//    }
//
//    public void setUsers(List<Map<String, String>> users) {
//        this.users = users;
//    }


}
