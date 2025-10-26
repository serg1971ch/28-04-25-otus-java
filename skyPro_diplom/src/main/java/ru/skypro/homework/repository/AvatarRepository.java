package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.entity.AvatarEntity;

import java.util.Optional;


public interface AvatarRepository extends JpaRepository<AvatarEntity,Long> {
    Optional<AvatarEntity> findImageByUserId(Integer id);
    Optional<AvatarEntity> findAvatarByUserId(Integer id);
}
