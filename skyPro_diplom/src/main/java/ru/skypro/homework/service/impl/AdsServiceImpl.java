package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.AvatarEntity;
import ru.skypro.homework.entity.ImageAdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageAdService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static java.nio.file.Paths.get;

@Slf4j
@Service
public class AdsServiceImpl implements AdsService {

    private final AdRepository adRepository;
    private final AdMapper adMapper;

    AdsServiceImpl(AdRepository adRepository,
                   AdMapper adMapper
                   ) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
    }
    /**
     * Возращает список всех объявлений.
     */
    @Override
    public AdsDto getAdAll() {
        log.info("Вы вызвали метод получения всех объявлений");
        List<AdDto> listAdDTO = adMapper.toListAdDTO(adRepository.findAll());
        AdsDto adsDto = new AdsDto();
        adsDto.setCount(listAdDTO.size());
        adsDto.setResults(listAdDTO);
        return adsDto;
    }

    /**
     * Возращает список объявлений пользователя.
     */
    @Override
    public AdsDto getAdsAuthorizedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<AdEntity> ads = adRepository.findByAuthorEmail(auth.getName());
        AdsDto adsDto = new AdsDto();
        adsDto.setCount(ads.size());
        adsDto.setResults(adMapper.toListAdDTO(ads));
        log.info("Вы нашли свои объявления");
        return adsDto;
    }

    /**
     * Возращает расширенную информацию по объявлению
     */
    @Override
    public ExtendedAdDto getAd(Integer id) {
        log.info("Вы вызвали метод получения информации об объявлении");
        AdEntity ad = adRepository.findById(id).orElseThrow(() -> {
            log.info("Объявление не найдено", AdNotFoundException.class);
            return new AdNotFoundException("Ad not found");
        });
        return adMapper.adToExtendedAd(ad);
    }

    /**
     * Удаляет объяевление.
     */
    @Override
    public void deleteAd(Integer id) {
        adRepository.deleteById(id);
        log.info("Объявление удалено");
    }

    /**
     * Обновление информации об объявлении.
     */
    @Override
    public AdDto updateAds(Integer id, CreateOrUpdateAdDto createOrUpdateAdDto) {
        AdEntity ad = adRepository.findById(id).orElseThrow(() -> {
            log.info("Объявление не найдено", AdNotFoundException.class);
            return new AdNotFoundException("Ad not found");
        });
        ad.setTitle(createOrUpdateAdDto.getTitle());
        ad.setPrice(createOrUpdateAdDto.getPrice());
        ad.setDescription(createOrUpdateAdDto.getDescription());
        log.info("Вы успешно изменили информацию в объявлении");
        return adMapper.toAdDto(adRepository.save(ad));
    }


    @Override
    public List<String> updateImage(Long id, MultipartFile image) {
        return List.of();
    }
}
