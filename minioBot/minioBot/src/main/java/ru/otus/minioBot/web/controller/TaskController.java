package ru.otus.minioBot.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.service.ImageServiceMinio;
import ru.otus.minioBot.web.dto.TaskImageDto;
import ru.otus.minioBot.web.mappers.TaskImageMapper;
import ru.otus.minioBot.web.mappers.TaskMapper;

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

