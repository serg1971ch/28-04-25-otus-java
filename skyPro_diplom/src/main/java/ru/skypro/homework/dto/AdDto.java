package ru.skypro.homework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdDto {

    @Schema(description = "id автора объявления", example = "0")
    Long author;
    @Schema(description = "ссылка на картинку объявления", example = "string")
    String image;
    @Schema(description = "id объявления", example = "0")
    Long pk;
    @Schema(description = "цена объявления",example = "0")
    Integer price;
    @Schema(description = "заголовок объявления", example = "string")
    String title;
}
