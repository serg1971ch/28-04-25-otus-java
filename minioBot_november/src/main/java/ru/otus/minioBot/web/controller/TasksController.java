package ru.otus.minioBot.web.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.repository.ImageRepository;
import ru.otus.minioBot.service.ImageServiceFS;
import ru.otus.minioBot.service.ImageServiceMinio;
import ru.otus.minioBot.service.NotificationService;
import ru.otus.minioBot.web.dto.ImageTaskDto;
import ru.otus.minioBot.web.dto.NotificationDto;
import ru.otus.minioBot.web.dto.TaskImageResponseDto;
import ru.otus.minioBot.web.service.NotificationDtoService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Log
@Controller
@RequestMapping("/app")
public class TasksController {
    private final NotificationDtoService service;
    private final ImageServiceMinio imageService;
    private final ImageRepository imageTaskRepository;
    Logger logger = Logger.getLogger(TasksController.class.getName());

    @Value("${app.download.dir:resources/photos}")
    private String downloadDirectory;

    public TasksController(NotificationDtoService service, ImageServiceMinio imageService, ImageRepository imageTaskRepository) {
        this.service = service;
        this.imageService = imageService;
        this.imageTaskRepository = imageTaskRepository;
    }

//    @RequestMapping("/list")
//    public String index(Model model) {
//        Iterable<Notification> noteIterable = service.getNotificationsForChat(864770685);
//        ArrayList<Notification> notes = new ArrayList<>((Collection<? extends Notification>) noteIterable);
//        model.addAttribute("notes", notes);
//        model.addAttribute("notesCount", notes.size());
//        model.addAttribute("comment", notes.stream().map(Notification::getComment).collect(Collectors.toList()));
//        log.info("notes{}" + notes);
//        return "index";
//    }

//    @RequestMapping("/list")
//    public String index(Model model) {
//        Iterable<NotificationDto> notificationDtos = service.findNotificationsByChatId(864770685);
//
//        ArrayList<NotificationDto> dtos = new ArrayList<>((Collection<? extends NotificationDto>) notificationDtos);
//        ArrayList<ImageTaskDto> imageTaskDtos = dtos.stream().map(NotificationDto::getImages).flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));
//
//        for(NotificationDto notificationDto : dtos) {
//            String base64String = notificationDto.getImages().get(1).getBase64Image(); // Или как вы получаете эту строку
//            log.info("Attempting to decode Base64 string (length {}):" + base64String.length()); // Логируем длину и саму строку
//        }
//
//        ArrayList<String> images = new ArrayList<>();
//        ArrayList<byte[]> imageBytes = new ArrayList<>();
//        for (ImageTaskDto imageTaskDto : imageTaskDtos) {
//            images.add(imageTaskDto.getPathFile().replace(downloadDirectory, "").replace('\\', '/'));
//            imageBytes.add(Base64.getDecoder().decode(imageTaskDto.getPathFile()));
//        }
//
//        model.addAttribute("notificationDtos", dtos); // Теперь dtos содержит Base64-строки для изображений
//        model.addAttribute("comment", dtos.stream().map(NotificationDto::getComment).collect(Collectors.toList()));
//        model.addAttribute("position", dtos.stream().map(NotificationDto::getPosition).collect(Collectors.toList()));
//        model.addAttribute("paths", images);
//        model.addAttribute("images", imageBytes);
//        log.info("list download images {}" + images.stream().toList());
//        return "index";
//    }

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

            // Исправленное логирование с проверкой на null
            String lengthInfo = (base64ImageFromDto != null) ? String.valueOf(base64ImageFromDto.length()) : "null";
            log.info(String.format("Attempting to decode Base64 string (length %s): %s", lengthInfo, base64ImageFromDto));;

            if (base64ImageFromDto == null || base64ImageFromDto.isEmpty()) {
                log.info("Base64 string for second image in NotificationDto (comment: '{}') is null or empty. Cannot decode." + notificationDto.getComment());
                // Здесь вы можете добавить логику для обработки отсутствующего Base64:
                // Например, добавить изображение-заглушку или пропустить его.
            }
            // else {
            //     try {
            //         byte[] decodedBytes = Base64.getDecoder().decode(base64ImageFromDto);
            //         // Используйте decodedBytes, если вам нужно декодировать здесь
            //     } catch (IllegalArgumentException e) {
            //         log.error("Failed to decode Base64 string for second image (comment: '{}'): {}", notificationDto.getComment(), e.getMessage());
            //     }
            // }
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
        model.addAttribute("paths", pathsForDisplay);
        model.addAttribute("images", decodedImageBytes); // Теперь это декодированные байты изображений

        log.info("List of downloaded image paths (for display): {}"+ pathsForDisplay); // Исправленное логирование
        return "index";
    }
}


