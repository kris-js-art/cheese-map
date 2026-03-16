package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/обновление категории")
public class CategoryRequestDto {

    @Schema(description = "Название категории", example = "Твердые сыры", required = true)
    @NotBlank(message = "Название категории обязательно")
    @Size(min = 2, max = 50, message = "Название категории должно содержать от 2 до 50 символов")
    private String name;

    @Schema(description = "Описание категории", example = "Сыры с низким содержанием влаги, длительной выдержкой")
    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
}