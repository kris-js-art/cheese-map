package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/обновление отзыва")
public class ReviewRequestDto {

    @Schema(description = "Имя автора отзыва", example = "Иван Петров", required = true)
    @NotBlank(message = "Имя автора обязательно")
    @Size(min = 2, max = 100, message = "Имя автора должно содержать от 2 до 100 символов")
    private String author;

    @Schema(description = "Рейтинг от 1 до 5", example = "5", required = true)
    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг должен быть не менее 1")
    @Max(value = 5, message = "Рейтинг должен быть не более 5")
    private Integer rating;

    @Schema(description = "Текст отзыва", example = "Очень вкусный сыр, рекомендую всем!")
    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String comment;
}