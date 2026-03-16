package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/обновление производителя")
public class ProducerRequestDto {

    @Schema(description = "Название производителя", example = "Parmigiano Reggiano", required = true)
    @NotBlank(message = "Название производителя обязательно")
    @Size(min = 2, max = 100, message = "Название производителя должно содержать от 2 до 100 символов")
    private String name;

    @Schema(description = "Страна производителя", example = "Италия", required = true)
    @NotBlank(message = "Страна обязательна")
    @Size(min = 2, max = 50, message = "Название страны должно содержать от 2 до 50 символов")
    private String country;

    @Schema(description = "Описание производителя",
            example = "Известный итальянский производитель сыров с вековой историей")
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;
}