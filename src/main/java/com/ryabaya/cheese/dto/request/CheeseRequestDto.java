package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/обновление сыра")
public class CheeseRequestDto {

    @Schema(description = "Название сыра", example = "Пармезан", required = true)
    @NotBlank(message = "Название сыра обязательно")
    @Size(min = 2, max = 100, message = "Название сыра должно содержать от 2 до 100 символов")
    private String name;

    @Schema(description = "Содержание жира в процентах", example = "32.5", required = true)
    @NotNull(message = "Содержание жира обязательно")
    @DecimalMin(value = "0.0", inclusive = false, message = "Содержание жира должно быть больше 0")
    @Digits(integer = 2, fraction = 2,
            message = "Содержание жира должно содержать не более 2 цифр и 2 знаков после запятой")
    private Double fats;

    @Schema(description = "Описание сыра", example = "Итальянский твердый сыр с богатым вкусом")
    @Size(max = 1000,
            message = "Описание не должно превышать 1000 символов")
    private String description;

    @Schema(description = "Цена в рублях", example = "1250.50", required = true)
    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    @DecimalMin(value = "0.01", message = "Цена должна быть не менее 0.01")
    @Digits(integer = 6, fraction = 2, message = "Цена должна содержать не более 6 цифр и 2 знаков после запятой")
    private Double price;
}