package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание сыра с категорией и отзывом")
public class CheeseCreationRequestDto {

    @Schema(description = "Название сыра", example = "Пармезан", required = true)
    @NotBlank(message = "Название сыра обязательно")
    @Size(min = 2, max = 100, message = "Название сыра должно содержать от 2 до 100 символов")
    private String name;

    @Schema(description = "Содержание жира в процентах", example = "32.5", required = true)
    @NotNull(message = "Содержание жира обязательно")
    @DecimalMin(value = "0.0", inclusive = false, message = "Содержание жира должно быть больше 0")
    @Digits(integer = 2,
            fraction = 2,
            message = "Содержание жира должно содержать не более 2 цифр и 2 знаков после запятой")
    private Double fats;

    @Schema(description = "Описание сыра", example = "Итальянский твердый сыр с богатым вкусом")
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @Schema(description = "Цена  в рублях", example = "1250.50", required = true)
    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @DecimalMin(value = "0.01", message = "Цена должна быть не менее 0.01")
    @Digits(integer = 6, fraction = 2, message = "Цена должна содержать не более 6 цифр и 2 знаков после запятой")
    private Double price;

    @Schema(description = "Название категории", example = "Твердые сыры", required = true)
    @NotBlank(message = "Название категории обязательно")
    @Size(min = 2, max = 50, message = "Название категории должно содержать от 2 до 50 символов")
    private String categoryName;

    @Schema(description = "Описание категории", example = "Сыры с низким содержанием влаги")
    @Size(max = 500, message = "Описание категории не должно превышать 500 символов")
    private String categoryDescription;

    @Schema(description = "Автор отзыва", example = "Иван Петров", required = true)
    @NotBlank(message = "Имя автора отзыва обязательно")
    @Size(min = 2, max = 100, message = "Имя автора должно содержать от 2 до 100 символов")
    private String reviewAuthor;

    @Schema(description = "Рейтинг от 1 до 5", example = "5", required = true)
    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг должен быть не менее 1")
    @Max(value = 5, message = "Рейтинг должен быть не более 5")
    private Integer reviewRating;

    @Schema(description = "Комментарий к отзыву", example = "Отличный сыр, очень вкусный!")
    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String reviewComment;

    @Schema(description = "Флаг для имитации проблемы", example = "false")
    private boolean initiatedProblem;
}