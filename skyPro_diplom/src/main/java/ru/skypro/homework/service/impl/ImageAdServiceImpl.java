package ru.skypro.homework.service.impl;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.ImageAdEntity;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.ImageException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageAdService;

import javax.transaction.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Paths.get;

@Slf4j
@Service
public class ImageAdServiceImpl implements ImageAdService {
    private final ImageRepository imageAdRepository;
    private final AdMapper adMapper;
    private final AdRepository adRepository;
    private final Path path;
    private final UserRepository userRepository;
    public ImageAdServiceImpl(ImageRepository imageAdRepository,
                              AdRepository adRepository,
                              @Value("/ads") String imagesDir,
                              AdMapper adMapper, UserRepository userRepository) {
        this.imageAdRepository = imageAdRepository;
        this.adRepository = adRepository;
        path = get(imagesDir);
        this.adMapper = adMapper;
        this.userRepository = userRepository;
    }

    /**
     * Создаёт объявления или меняет фотографию объявления.
     */
    @Override
    @Transactional
    public AdDto createAd(AdDto adDto, MultipartFile multipartFile, String userName) throws IOException {

        AdEntity ad = adRepository.save(adMapper.toAd(adDto));
        ad.setAuthor(userRepository.findByEmail(userName).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("Ad");
        }));
        ad.setImage(updateAdImage(ad.getId(), multipartFile));
        adRepository.save(ad);
        log.info("Вы успешно создали объявление");
        return adMapper.toAdDto(ad);
    }

    /**
     * Сохранеет или меняет фотографию в объявлении.
     */
    @Override
    @Transactional
    public ImageAdEntity updateAdImage(Integer id, MultipartFile multipartFile) {
        log.info("Вы вызвали метод обновления картинки у объявления");
        AdEntity ad = adRepository.findById(id).orElseThrow(() -> {
            log.info("Объявление не найдено", AdNotFoundException.class);
            return new AdNotFoundException("Ad not found");
        });
        ImageAdEntity image = imageAdRepository.findImageAdByAdId(id).orElse(new ImageAdEntity());

        try {
            byte[] data = multipartFile.getBytes();
            String extension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            Path imagePath = path.resolve(id + "." + extension);
            Files.write(imagePath, data);

            image.setAd(ad);
            image.setPath(imagePath.toString());
            image.setFileSize(multipartFile.getSize());
            image.setMediaType(multipartFile.getContentType());

        } catch (IOException e) {
            log.info("Ошибка ввода-вывода изображения объявления: " + e.getMessage());
            throw new ImageException();
        }
        imageAdRepository.save(image);
        ad.setImage(image);
        adRepository.save(ad);
        log.info("Вы успешно обновили картинку в объявлении");
        return image;
    }

    /**
     * Возращает картинку объявления в виде массива байт
     */
    @Override
    @SneakyThrows
    public byte[] getImageAd(Integer id) throws IOException {
        ImageAdEntity imageAd = imageAdRepository.findImageAdByAdId(id).orElseThrow(() -> {
            log.info("Пользователь не найден", UserNotFoundException.class);
            return new UserNotFoundException("not");

        });
        String path = imageAd.getPath();
        return Files.readAllBytes(Paths.get(path));
    }
}
