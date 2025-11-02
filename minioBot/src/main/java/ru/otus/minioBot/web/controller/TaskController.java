package ru.otus.minioBot.web.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.otus.minioBot.service.ImageServiceMinio;
import ru.otus.minioBot.web.dto.TaskImageDto;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final ImageServiceMinio taskService;
//    private final TaskImageMapper taskImageMapper;

    public TaskController(ImageServiceMinio taskService) {
        this.taskService = taskService;
//        this.taskImageMapper = taskImageMapper;
    }

    @PostMapping("/{id}/image")
//    @Operation(summary = "Upload image to task")
//    @PreAuthorize("canAccessTask(#id)")
    public void uploadImage(@PathVariable final Long id,
            @Validated @ModelAttribute final TaskImageDto imageDto) {
//        ImageTask image = taskImageMapper.toEntity(imageDto);
//        taskService.upload();
    }
}

