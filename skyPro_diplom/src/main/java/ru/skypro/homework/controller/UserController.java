package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.service.AvatarService;
import ru.skypro.homework.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@RequestMapping("/users")
@CrossOrigin(value = "http://localhost:3000")
@Tag(name = "Пользователи")
@RestController
public class UserController {

    private final UserService userService;
    private final AvatarService avatarService;

    public UserController(UserService userService,
             AvatarService avatarService
    ) {

        this.userService = userService;
        this.avatarService = avatarService;

    }

    @Operation(summary = "Обновление пароля", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content())
    })
    @PreAuthorize("hasRole('ADMIN') or @userService.isUserName(principal.username, #email)")
    @PostMapping("/set_password")
    public ResponseEntity<?> setPassword(@RequestBody NewPasswordDto newPassword) {
        log.info("The setPassword method of UserController is called");
        userService.setNewPassword(newPassword);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получение информации об авторизованном пользователе", responses = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class)
                    )),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())
    })

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }

    @Operation(summary = "Обновление информации об авторизованном пользователе",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UpdateUserDto.class),
                                    examples = @ExampleObject(
                                            description = "Информация о пользователе обновлена"
                                    )
                            )),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthorized",
                            content = @Content())
            })
    @PatchMapping(value = "/me")
    public ResponseEntity<UpdateUserDto> updateUser(@RequestBody UpdateUserDto userPatch) {
        log.info("The updateUser method of UserController is called");
        return ResponseEntity.ok(userService.updateUser(userPatch));
    }


    @Operation(summary = "Обновление аватара авторизованного пользователя", responses = {
            @ApiResponse(responseCode = "200",
                    description = "OK",
                    content = @Content()),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content())
    })
    @PatchMapping("/me/image")
    @PreAuthorize("hasRole('ADMIN') or @userService.isUserName(principal.username, #email)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public void updateImage(@RequestBody MultipartFile image) throws IOException {
        log.info("Вызван метод контролера обновление аватара пользователя");

        avatarService.updateImage(image);
    }

    @GetMapping(value = "/me/image/{id}", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, "image/*"})
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long id) throws IOException {
        log.info("Вызван метод контролера возращаюший массив байт аватара");
        return avatarService.getAvatar(id);
    }
}
