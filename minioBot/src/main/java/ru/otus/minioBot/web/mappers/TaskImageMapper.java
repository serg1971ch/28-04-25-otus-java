package ru.otus.minioBot.web.mappers;

import org.mapstruct.Mapper;
import ru.otus.minioBot.model.ImageTask;
import ru.otus.minioBot.web.dto.TaskImageDto;

@Mapper(componentModel = "spring")
public interface TaskImageMapper extends Mappable<ImageTask, TaskImageDto> {
}
