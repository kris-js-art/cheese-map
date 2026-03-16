package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.ReviewRequestDto;
import com.ryabaya.cheese.dto.response.ReviewResponseDto;
import com.ryabaya.cheese.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Отзывы", description = "Управление отзывами на сыры")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/cheese/{cheeseId}")
    @Operation(summary = "Создать новый отзыв", description = "Создает отзыв для указанного сыра")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Отзыв успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Сыр не найден")
    })
    public ResponseEntity<ReviewResponseDto> createReview(
            @Parameter(description = "ID сыра", required = true) @PathVariable Long cheeseId,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        return new ResponseEntity<>(reviewService.createReview(cheeseId, reviewDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить отзыв по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв найден"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    })
    public ResponseEntity<ReviewResponseDto> getReviewById(
            @Parameter(description = "ID отзыва", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/cheese/{cheeseId}")
    @Operation(summary = "Получить все отзывы для сыра")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список отзывов получен"),
            @ApiResponse(responseCode = "404", description = "Сыр не найден")
    })
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByCheeseId(
            @Parameter(description = "ID сыра", required = true) @PathVariable Long cheeseId) {
        return ResponseEntity.ok(reviewService.getReviewsByCheeseId(cheeseId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить отзыв")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    })
    public ResponseEntity<ReviewResponseDto> updateReview(
            @Parameter(description = "ID отзыва", required = true) @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отзыв удален"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID отзыва", required = true) @PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}