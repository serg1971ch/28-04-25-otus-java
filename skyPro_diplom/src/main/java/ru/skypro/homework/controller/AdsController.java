package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;
import ru.skypro.homework.entity.ImageAdEntity;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageAdService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ads")
@CrossOrigin("http://localhost:3000")
public class AdsController {
    private final AdsService adsService;
    private final ImageAdService imageAdService;


    public AdsController(AdsService adsService,
                         ImageAdService imageAdService) {
        this.adsService = adsService;
        this.imageAdService = imageAdService;
    }


    @Operation(summary = "Получение всех объявлений", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = AdsDto.class)))})
    @GetMapping
    public ResponseEntity<AdsDto> getAds() {
        log.info("The getAds method of AdsController is called");
        return ResponseEntity.ok().body(adsService.getAdAll());
    }

    @Operation(summary = "Добавление объявления", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AdDto.class))),

            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())})
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AdDto> createAd(@RequestPart(value = "properties") @Valid AdDto ad,
                                          @RequestPart(value = "image") MultipartFile image,
                                          Authentication authentication) throws IOException {
        log.info("The createAd method of AdsController is called");
        return ResponseEntity.status(HttpStatus.CREATED).body(imageAdService.createAd(ad, image, authentication.getName()));
    }

    @Operation(summary = "Получение информации об объявлении", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExtendedAdDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content())})
    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAdDto> getAdsExtended(@PathVariable("id") Integer id) {
        log.info("The getAdsExtended method of AdsController is called");
        return ResponseEntity.ok().body(adsService.getAd(id));
    }

    @Operation(summary = "Удаление объявления", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")})
    @PreAuthorize("@checkAccessService.isAdminOrOwnerAd(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAds(@PathVariable("id") Integer id) {
        log.info("The deleteAds method of AdsController is called");
        adsService.deleteAd(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновление информации об объявлении", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content()),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content())
    })
    @PreAuthorize("@checkAccessService.isAdminOrOwnerAd(#id, authentication)")
    @PatchMapping("/{id}")
    public ResponseEntity<AdDto> updateAds(@PathVariable("id") Integer id, @RequestBody CreateOrUpdateAdDto createOrUpdateAd) {
        return ResponseEntity.ok().body(adsService.updateAds(id, createOrUpdateAd));
    }

    @Operation(summary = "Получение объявлений авторизованного пользователя", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdsDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content())
    })
    @GetMapping("/me")
    public ResponseEntity<AdsDto> getAdsAuthorizedUser() {
        return ResponseEntity.ok().body(adsService.getAdsAuthorizedUser());
    }

    @Operation(summary = "Обновление картинки объявления", tags = {"Объявления"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/octet-stream",
                            array = @ArraySchema(schema = @Schema(implementation = byte[].class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content())
    })

    @PreAuthorize("@checkAccessService.isAdminOrOwnerAd(#id, authentication)")
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageAdEntity> updateImage(@PathVariable("id") Integer id,
                                                     @RequestPart("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok().body(imageAdService.updateAdImage(id, image));
    }


    @GetMapping(value = "/img/{adId}", produces = {MediaType.IMAGE_PNG_VALUE, "image/*"})
    public byte[] getImageAd(@PathVariable Integer adId) throws IOException {
        log.info("Вызван метод контролера возращаюший массив байт изображения объявления");
        return imageAdService.getImageAd(adId);
    }
}
