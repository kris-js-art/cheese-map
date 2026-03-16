package com.ryabaya.cheese.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с данными категории")
public class CategoryResponseDto {

    @Schema(description = "ID категории", example = "1")
    private Long id;

    @Schema(description = "Название категории", example = "Твердые сыры")
    private String name;

    @Schema(description = "Описание категории", example = "Сыры с низким содержанием влаги, длительной выдержкой")
    private String description;
}