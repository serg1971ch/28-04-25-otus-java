package ru.upmt.webServerBot.web.mappers;

import java.util.List;
import java.util.stream.Collectors;

public interface Mappable<E, D> {

    D toDto(E entity);

    default List<D> toDto(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    E toEntity(D dto);

}
