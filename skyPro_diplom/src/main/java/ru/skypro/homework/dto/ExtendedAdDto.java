package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtendedAdDto {
    @Schema(description = "id объявления")
    Long pk;
    @Schema(description = "имя автора объявления")
    String authorFirstName;
    @Schema(description = "фамилия автора объявления")
    String authorLastName;
    @Schema(description = "описание объявления")
    String description;
    @Schema(description = "логин автора объявления")
    String email;
    @Schema(description = "ссылка на картинку объявления")
    String image;
    @Schema(description = "телефон автора объявления")
    String phone;
    @Schema(description = "цена объявления")
    int price;
    @Schema(description = "заголовок объявления")
    String title;


}
