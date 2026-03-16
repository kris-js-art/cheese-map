package com.ryabaya.cheese.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с данными производителя")
public class ProducerResponseDto {

    @Schema(description = "ID производителя", example = "1")
    private Long id;

    @Schema(description = "Название производителя", example = "Parmigiano Reggiano")
    private String name;

    @Schema(description = "Страна производителя", example = "Италия")
    private String country;

    @Schema(description = "Описание производителя",
            example = "Известный итальянский производитель сыров с вековой историей")
    private String description;
}