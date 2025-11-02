package ru.otus.minioBot.web.mappers;

import org.mapstruct.Mapper;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.web.dto.NotificationDto;

@Mapper(componentModel = "spring")
public interface TaskMapper extends Mappable<Notification, NotificationDto> {
}
