package com.ryabaya.cheese.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с данными отзыва")
public class ReviewResponseDto {

    @Schema(description = "ID отзыва", example = "1")
    private Long id;

    @Schema(description = "Имя автора", example = "Иван Петров")
    private String author;

    @Schema(description = "Рейтинг", example = "5")
    private Integer rating;

    @Schema(description = "Текст отзыва", example = "Очень вкусный сыр, рекомендую всем!")
    private String comment;

    @Schema(description = "Дата создания", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}