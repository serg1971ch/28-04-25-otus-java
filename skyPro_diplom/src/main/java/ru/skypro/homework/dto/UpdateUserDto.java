package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UpdateUserDto {
    @Schema(description = "Имя пользователя", minLength = 3, maxLength = 10)
    String firstName;
    @Schema(minLength = 3, maxLength = 10, description = "фамилия пользователя")
    String lastName;
    @Schema(pattern = "\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}", description = "телефон пользователя")
    String phone;

    public UpdateUserDto() {

    }
}
