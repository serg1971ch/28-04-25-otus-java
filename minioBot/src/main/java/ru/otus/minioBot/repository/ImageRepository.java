package ru.otus.minioBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.otus.minioBot.model.ImageTask;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageTask, Long>, PagingAndSortingRepository<ImageTask, Long> {
    Optional<ImageTask> findByNotification_Id(Long id);

    @Override
    Optional<ImageTask> findById(Long aLong);
}
