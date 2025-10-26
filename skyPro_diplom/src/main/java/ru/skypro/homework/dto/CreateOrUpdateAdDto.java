package ru.skypro.homework.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrUpdateAdDto {
    @Schema(minLength = 4, maxLength = 32, description = "заголовок объявления")
    String title;
    @Schema(minimum = "0", maximum = "1000000", description = "цена объявления")
    int price;
    @Schema(minLength = 8, maxLength = 63, description = "описание объявления")
    String description;
}
