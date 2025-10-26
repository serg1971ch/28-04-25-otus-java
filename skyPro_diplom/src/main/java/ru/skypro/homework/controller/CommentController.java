package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;
import ru.skypro.homework.service.CommentService;

@Slf4j
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Комментарии")
@CrossOrigin("http://localhost:3000")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Получение комментариев объявления", responses = {
            @ApiResponse(responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentsDto.class)
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "404",
                    description = "Not found",
                    content = @Content())
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<CommentsDto> get(@PathVariable("id") Integer id) {
        log.info("The get method of CommentController is called");
        return ResponseEntity.ok(commentService.get(id));
    }

    @Operation(summary = "Добавление комментария к объявлению", responses = {
            @ApiResponse(responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentDto.class)
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "404",
                    description = "Not found",
                    content = @Content())
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> create(@PathVariable("id") Integer id,
                                             @RequestBody CreateOrUpdateCommentDto newComment,
                                             Authentication authentication) {
        log.info("The create method of CommentController is called");
        return ResponseEntity.ok(commentService.create(id, newComment,authentication.getName()));
    }

    @Operation(summary = "Удаление комментария", responses = {
            @ApiResponse(responseCode = "200",
                    description = "OK",
                    content = @Content()),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = @Content()),
            @ApiResponse(responseCode = "404",
                    description = "Not found",
                    content = @Content())
    })
    @PreAuthorize("@checkAccessService.isAdminOrOwnerComment(#adId, #commentId, authentication)")
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<?> delete(@PathVariable(value = "adId") Integer adId,
                                    @PathVariable(value = "commentId") Integer commentId) {
        log.info("The delete method of CommentController is called");
        commentService.delete(adId, commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновление комментария", responses = {
            @ApiResponse(responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentDto.class)
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content()),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = @Content()),
            @ApiResponse(responseCode = "404",
                    description = "Not found",
                    content = @Content())
    })
    @PreAuthorize("@checkAccessService.isAdminOrOwnerComment(#adId, #commentId, authentication)")
    @PatchMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<CommentDto> update(@PathVariable(value = "adId") Integer adId,
                                             @PathVariable(value = "commentId") Integer commentId,
                                             @RequestBody CreateOrUpdateCommentDto newComment) {
        log.info("The update method of CommentController is called");
        return ResponseEntity.ok(commentService.update(adId, commentId, newComment));

    }
}
