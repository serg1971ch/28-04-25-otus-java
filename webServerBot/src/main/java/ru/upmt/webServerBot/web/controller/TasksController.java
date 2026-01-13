package ru.upmt.webServerBot.web.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.repository.ImageRepository;
import ru.upmt.webServerBot.service.UserService;
import ru.upmt.webServerBot.web.dto.ImageTaskDto;
import ru.upmt.webServerBot.web.dto.NotificationDto;
import ru.upmt.webServerBot.web.dto.UserDto;
import ru.upmt.webServerBot.web.service.NotificationDtoService;


import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Log
@Controller
@RequestMapping("/app")
public class TasksController {
    private final NotificationDtoService service;
//    private final ImageRepository imageTaskRepository;
    Logger logger = Logger.getLogger(TasksController.class.getName());
//    private final UserService userService;
    List<UserDto> users;


    @Value("${app.download.dir:resources/photos}")
    private String downloadDirectory;

    public TasksController(NotificationDtoService service
//            ,
//                           ImageRepository imageTaskRepository, UserService userService
    ) {
        this.service = service;
//        this.imageTaskRepository = imageTaskRepository;
//        this.userService = userService;
    }

    @RequestMapping("/list")
    public String index(Model model) {
        Iterable<NotificationDto> notificationDtosIterable = service.findNotificationsByChatId(864770685);

        // Преобразуем Iterable в ArrayList
        ArrayList<NotificationDto> dtos = new ArrayList<>();
        notificationDtosIterable.forEach(dtos::add); // Более современный способ

        // Логирование и проверка для каждого NotificationDto
        for (NotificationDto notificationDto : dtos) {
            if (notificationDto.getImages() == null || notificationDto.getImages().size() <= 1) {
                log.info("NotificationDto с комментарием '{}' не имеет хотя бы двух изображений. Пропускаем обработку второго изображения." + notificationDto.getComment());
                continue; // Пропустить текущий notificationDto
            }

            String base64ImageFromDto = notificationDto.getImages().get(1).getBase64Image();
//            NotificationDto noteDto = service.getUserDto(notificationDto.getId());
//            users = noteDto.getUsers();
//            logger.info("users: " + users.stream().map(UserDto::toString).collect(Collectors.joining(",")));

            try {
                // Получаем пользователей для текущей заметки
                List<UserDto> usersForNote = service.getUserDto(notificationDto.getId()).getUsers();
                // Устанавливаем либо полученный список, либо пустой (но не null)
                notificationDto.setUsers(usersForNote != null ? usersForNote : new ArrayList<>());
            } catch (Exception e) {
                log.warning("Failed to load users for notification " + notificationDto.getId() + ": " + e.getMessage());
                // В случае ошибки — пустой список
                notificationDto.setUsers(new ArrayList<>());
            }

            // Исправленное логирование с проверкой на null
            String lengthInfo = (base64ImageFromDto != null) ? String.valueOf(base64ImageFromDto.length()) : "null";
//            log.info(String.format("Attempting to decode Base64 string (length %s): %s", lengthInfo, base64ImageFromDto));;

            if (base64ImageFromDto == null || base64ImageFromDto.isEmpty()) {
                log.info("Base64 string for second image in NotificationDto (comment: '{}') is null or empty. Cannot decode." + notificationDto.getComment());
            }
        }

        // Собираем все ImageTaskDto из всех NotificationDto
        ArrayList<ImageTaskDto> allImageTaskDtos = dtos.stream()
                .map(NotificationDto::getImages)
                .filter(java.util.Objects::nonNull) // Отфильтровываем null списки изображений
                .flatMap(Collection::stream)
                .filter(java.util.Objects::nonNull) // Отфильтровываем null ImageTaskDto
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<String> pathsForDisplay = new ArrayList<>();
        ArrayList<byte[]> decodedImageBytes = new ArrayList<>();

        for (ImageTaskDto imageTaskDto : allImageTaskDtos) {
            // *** КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: Используем base64Image, а не pathFile для декодирования ***
            if (imageTaskDto.getBase64Image() != null && !imageTaskDto.getBase64Image().isEmpty()) {
                try {
                    decodedImageBytes.add(Base64.getDecoder().decode(imageTaskDto.getBase64Image()));
                    // pathFile по-прежнему используется для отображения пути, если он есть
                    if (imageTaskDto.getPathFile() != null) {
                        pathsForDisplay.add(imageTaskDto.getPathFile().replace(downloadDirectory, "").replace('\\', '/'));
                    } else {
                        pathsForDisplay.add("no_path_available"); // Заглушка, если путь отсутствует
                    }
                } catch (IllegalArgumentException e) {
                    log.info("Failed to decode Base64 string for ImageTaskDto (ID: {}). Base64 string prefix: '{}...'. Error: {}" +
                            imageTaskDto.getId() + imageTaskDto.getBase64Image().substring(0, Math.min(imageTaskDto.getBase64Image().length(), 50)) +
                            e.getMessage() + e); // Передаем исключение, чтобы получить stack trace
                    decodedImageBytes.add(new byte[0]); // Добавляем пустой массив байтов как заглушку
                    pathsForDisplay.add("error_decoding_image");
                }
            } else {
                log.info("ImageTaskDto (ID: {}) не содержит Base64 данных изображения. PathFile был: {}" + imageTaskDto.getId() + imageTaskDto.getPathFile());
                decodedImageBytes.add(new byte[0]); // Заглушка
                pathsForDisplay.add("no_base64_data");
            }
        }

        model.addAttribute("notificationDtos", dtos);

        model.addAttribute("comment", dtos.stream().map(NotificationDto::getComment).collect(Collectors.toList()));
        model.addAttribute("position", dtos.stream().map(NotificationDto::getPosition).collect(Collectors.toList()));
//        model.addAttribute("paths", pathsForDisplay);
        model.addAttribute("users", dtos.stream().map(NotificationDto::getUsers).collect(Collectors.toList()));
        model.addAttribute("images", decodedImageBytes); // Теперь это декодированные байты изображений
        log.info("List Users: " + dtos.stream().map(NotificationDto::getUsers).toList());
//        log.info("List of downloaded image paths (for display): {}" + pathsForDisplay);
        return "index";
    }
}


