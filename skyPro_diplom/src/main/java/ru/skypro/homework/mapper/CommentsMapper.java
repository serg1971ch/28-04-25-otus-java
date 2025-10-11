package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.skypro.homework.constants.Constants;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;
import ru.skypro.homework.entity.CommentEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentsMapper {

    @Mappings({
            @Mapping(target = "pk", source = "id"),
            @Mapping(target = "author", source = "author.id"),
            @Mapping(expression = "java(buildImageUrl(comment.getAuthor().getId()))", target = "authorImage"),
            @Mapping(target = "authorFirstName", source = "author.firstName"),
            @Mapping(target = "createdAt", source = "createdAt")
    })
    CommentDto commentToCommentDTO(CommentEntity comment);

    List<CommentDto> commentsToCommentsDTO(List<CommentEntity> comments);

    CreateOrUpdateCommentDto commentToCreateOrUpdateCommentDto(CommentEntity comment);

    default String buildImageUrl(Long id) {
        return Constants.PATH_IMAGE + id;
    }

    default long dateToMillis(Date date, TimeZone timeZone) {
        if (date == null) {
            return 0;
        }
        // Создаем Calendar с указанным часовым поясом
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        // Возвращаем время в миллисекундах с учетом часового пояса
        return calendar.getTimeInMillis();
    }

    default long localDateTimeToMillis(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0L; // Возвращаем 0, если дата null
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
