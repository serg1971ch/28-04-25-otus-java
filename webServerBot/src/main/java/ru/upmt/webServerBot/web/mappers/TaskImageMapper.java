package ru.upmt.webServerBot.web.mappers;

import org.mapstruct.Mapper;
import ru.upmt.webServerBot.model.ImageTask;
import ru.upmt.webServerBot.web.dto.TaskImageDto;


@Mapper(componentModel = "spring")
public interface TaskImageMapper extends Mappable<ImageTask, TaskImageDto> {
}
