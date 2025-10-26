package ru.otus.minioBot.web.controller;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TaskController {

    private final ImageServiceMinio taskService;
    private final TaskMapper taskMapper;
    private final TaskImageMapper taskImageMapper;

//    @PutMapping
//    @MutationMapping(name = "updateTask")
//    @Operation(summary = "Update task")
//    @PreAuthorize("canAccessTask(#dto.id)")
//    public TaskDto update(
//            @Validated(OnUpdate.class)
//            @RequestBody @Argument final TaskDto dto
//    ) {
//        Task task = taskMapper.toEntity(dto);
//        Task updatedTask = taskService.update(task);
//        return taskMapper.toDto(updatedTask);
//    }
//
//    @GetMapping("/{id}")
//    @QueryMapping(name = "taskById")
//    @Operation(summary = "Get TaskDto by id")
//    @PreAuthorize("canAccessTask(#id)")
//    public TaskDto getById(
//            @PathVariable @Argument final Long id
//    ) {
//        Task task = taskService.getById(id);
//        return taskMapper.toDto(task);
//    }
//
//    @DeleteMapping("/{id}")
//    @MutationMapping(name = "deleteTask")
//    @Operation(summary = "Delete task")
//    @PreAuthorize("canAccessTask(#id)")
//    public void deleteById(
//            @PathVariable @Argument final Long id
//    ) {
//        taskService.delete(id);
//    }

    @PostMapping("/{id}/image")
//    @Operation(summary = "Upload image to task")
//    @PreAuthorize("canAccessTask(#id)")
    public void uploadImage(
            @PathVariable final Long id,
            @Validated @ModelAttribute final TaskImageDto imageDto
    ) {
        ImageTask image = taskImageMapper.toEntity(imageDto);
//        taskService.upload();
    }
}

