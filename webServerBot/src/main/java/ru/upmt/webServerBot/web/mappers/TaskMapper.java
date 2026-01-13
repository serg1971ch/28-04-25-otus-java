package ru.upmt.webServerBot.web.mappers;

import org.mapstruct.Mapper;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.web.dto.NotificationDto;

@Mapper(componentModel = "spring")
public interface TaskMapper extends Mappable<Notification, NotificationDto> {
}
