package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdsServiceImplTest {
    @Mock
    private AdRepository adRepository;

    @Mock
    private AdMapper adMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;


    @InjectMocks
    private AdsServiceImpl adsService;

    private List<AdEntity> ads;
    private List<AdDto> adDtos;

    @BeforeEach
    public void setUp() {
        AdEntity ad1 = new AdEntity();
        AdEntity ad2 = new AdEntity();
        ads = Arrays.asList(ad1, ad2);

        AdDto adDto1 = new AdDto();
        AdDto adDto2 = new AdDto();
        adDtos = Arrays.asList(adDto1, adDto2);

        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    public void testGetAdAll() {
        when(adRepository.findAll()).thenReturn(ads);
        when(adMapper.toListAdDTO(ads)).thenReturn(adDtos);

        AdsDto result = adsService.getAdAll();

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(adDtos.size());
        assertThat(result.getResults()).isEqualTo(adDtos);

        verify(adRepository, times(1)).findAll();
        verify(adMapper, times(1)).toListAdDTO(ads);
    }


    @Test
    public void testGetAdsAuthorizedUser() {
        String userEmail = "test@example.com";
        AdEntity ad1 = new AdEntity();
        AdEntity ad2 = new AdEntity();
        List<AdEntity> ads = Arrays.asList(ad1, ad2);

        AdDto adDTO1 = new AdDto();
        AdDto adDTO2 = new AdDto();
        List<AdDto> adDTOs = Arrays.asList(adDTO1, adDTO2);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.getName()).thenReturn(userEmail);
        when(adRepository.findByAuthorEmail(userEmail)).thenReturn(ads);
        when(adMapper.toListAdDTO(ads)).thenReturn(adDTOs);

        AdsDto result = adsService.getAdsAuthorizedUser();

        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getResults()).isEqualTo(adDTOs);
    }

    @Test
    public void testGetAdPositive() {
        Integer adId = 1;
        AdEntity adEntity = new AdEntity();
        ExtendedAdDto expectedDto = new ExtendedAdDto();

        when(adRepository.findById(adId)).thenReturn(Optional.of(adEntity));
        when(adMapper.adToExtendedAd(adEntity)).thenReturn(expectedDto);

        ExtendedAdDto result = adsService.getAd(adId);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    public void testGetAdNegative() {
        Integer adId = 1;

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adsService.getAd(adId))
                .isInstanceOf(AdNotFoundException.class)
                .hasMessage("Ad not found");
    }

    @Test
    public void testDeleteAd() {
        Integer adId = 1;
        adsService.deleteAd(adId);
        verify(adRepository, times(1)).deleteById(adId);
    }

    @Test
    public void testUpdateAdsPositive() {
        AdEntity adEntity = new AdEntity();
        adEntity.setId(1);
        adEntity.setTitle("Old Title");
        adEntity.setPrice(100);
        adEntity.setDescription("Old Description");

         CreateOrUpdateAdDto createOrUpdateAdDto = new CreateOrUpdateAdDto();
        createOrUpdateAdDto.setTitle("New Title");
        createOrUpdateAdDto.setPrice(200);
        createOrUpdateAdDto.setDescription("New Description");

        AdDto expectedAdDto = new AdDto();
        expectedAdDto.setTitle("New Title");
        expectedAdDto.setPrice(200);

        when(adRepository.findById(1)).thenReturn(Optional.of(adEntity));
        when(adRepository.save(any(AdEntity.class))).thenReturn(adEntity);
        when(adMapper.toAdDto(adEntity)).thenReturn(expectedAdDto);

        AdDto result = adsService.updateAds(1, createOrUpdateAdDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getPrice()).isEqualTo(200);

        verify(adRepository, times(1)).findById(1);
        verify(adRepository, times(1)).save(adEntity);
        verify(adMapper, times(1)).toAdDto(adEntity);
    }

    @Test
    public void testUpdateAdsNegative() {
        CreateOrUpdateAdDto createOrUpdateAdDto = new CreateOrUpdateAdDto();
        createOrUpdateAdDto.setTitle("New Title");
        createOrUpdateAdDto.setPrice(200);
        createOrUpdateAdDto.setDescription("New Description");

        when(adRepository.findById(1)).thenReturn(Optional.empty());

        try {
            adsService.updateAds(1, createOrUpdateAdDto);
        } catch (AdNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("Ad not found");
        }

        verify(adRepository, times(1)).findById(1);
        verify(adRepository, never()).save(any(AdEntity.class));
        verify(adMapper, never()).toAdDto(any(AdEntity.class));
    }
}