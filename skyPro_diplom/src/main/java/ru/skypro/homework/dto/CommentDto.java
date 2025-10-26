package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    @Schema(description = "id автора комментария")
    long author;
    @Schema(description = "ссылка на аватар автора комментария")
    String authorImage;
    @Schema(description = "имя создателя комментария")
    String authorFirstName;
    @Schema(description = "дата и время создания комментария в миллисекундах с 00:00:00 01.01.1970")
    long createdAt;



    @Schema(description = "id комментария")
    long pk;
    @Schema(description = "текст комментария")
    String text;
}
