package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.entity.ImageAdEntity;

import java.util.Optional;


public interface ImageRepository extends JpaRepository<ImageAdEntity, Long> {
    Optional<ImageAdEntity> findImageAdByAdId(Integer id);

}
