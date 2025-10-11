package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewPasswordDto {
    @Schema(maxLength = 16, minLength = 8, description = "текущий пароль")
    String currentPassword;
    @Schema(maxLength = 16, minLength = 8, description = "новый пароль")
    String newPassword;
}

