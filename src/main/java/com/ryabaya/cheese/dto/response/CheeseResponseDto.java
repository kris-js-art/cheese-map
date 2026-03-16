package com.ryabaya.cheese.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "Ответ с данными сыра")
public class CheeseResponseDto {

    @Schema(description = "ID сыра", example = "1")
    private Long id;

    @Schema(description = "Название сыра", example = "Пармезан")
    private String name;

    @Schema(description = "Содержание жира в процентах", example = "32.5")
    private Double fats;

    @Schema(description = "Описание сыра", example = "Итальянский твердый сыр с богатым вкусом")
    private String description;

    @Schema(description = "Цена в рублях", example = "1250.50")
    private Double price;

    @Schema(description = "Производитель сыра")
    private ProducerResponseDto producer;

    @Schema(description = "Категории сыра")
    private Set<CategoryResponseDto> categories;

    @Schema(description = "Отзывы на сыр")
    private List<ReviewResponseDto> reviews;
}