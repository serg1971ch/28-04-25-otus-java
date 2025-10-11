package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.List;

public interface AdsService {

    AdsDto getAdAll();
    ExtendedAdDto getAd(Integer id);
    void deleteAd(Integer id);
    List<String> updateImage(Long id, MultipartFile image);
    AdsDto getAdsAuthorizedUser();
    AdDto updateAds(Integer id, CreateOrUpdateAdDto createOrUpdateAd);
}
