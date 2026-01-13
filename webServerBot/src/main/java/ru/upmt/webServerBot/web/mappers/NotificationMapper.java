package ru.upmt.webServerBot.web.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.upmt.webServerBot.model.Notification;
import ru.upmt.webServerBot.web.dto.ImageTaskDto;
import ru.upmt.webServerBot.web.dto.NotificationDto;

import java.util.Collections;

@Component
public class NotificationMapper implements Mappable<Notification, NotificationDto> {

    private final ImageTaskMapper imageTaskMapper; // Для преобразования связанных ImageTask

    @Autowired
    public NotificationMapper(ImageTaskMapper imageTaskMapper) {
        this.imageTaskMapper = imageTaskMapper;
    }

    @Override
    public NotificationDto toDto(Notification entity) {
        if (entity == null) {
            return null;
        }
        NotificationDto dto = new NotificationDto();
        dto.setPosition(entity.getPosition());
        dto.setComment(entity.getComment());
        dto.setNotificationDate(entity.getNotificationDate());
        dto.setId(entity.getId());
        // Преобразуем список ImageTask в список ImageTaskDto
        dto.setImages(imageTaskMapper.toDto(entity.getImageTasks()));

        return dto;
    }

    @Override
    public Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }
        Notification entity = new Notification();
        entity.setPosition(dto.getPosition());
        entity.setComment(dto.getComment());
//        entity.setTaskComplete(dto.getTaskComplete());
//        entity.setStatus(dto.getStatus());
        entity.setSentDate(dto.getSentDate());
        entity.setImageTasks(Collections.singletonList(imageTaskMapper.toEntity((ImageTaskDto) dto.getImages())));
        // Преобразуем список ImageTaskDto в список ImageTask
//        if (dto.getImages() != null) {
//            entity.setImageTasks(Collections.singletonList(imageTaskMapper.toEntity((ImageTaskDto) dto.getImages())));
            // !!! Важно: установите обратную ссылку notification для каждого ImageTask !!!
//            entity.getImageTasks().forEach(imageTask -> imageTask.setNotification(entity));
//        }

        return entity;
    }
}
