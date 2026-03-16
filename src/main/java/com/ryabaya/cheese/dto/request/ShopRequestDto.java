package com.ryabaya.cheese.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/обновление магазина")
public class ShopRequestDto {

    @Schema(description = "Название магазина", example = "Сырная лавка", required = true)
    @NotBlank(message = "Название магазина обязательно")
    @Size(min = 2, max = 100, message = "Название магазина должно содержать от 2 до 100 символов")
    private String name;

    @Schema(description = "Адрес магазина", example = "ул. Пушкина, д. 10", required = true)
    @NotBlank(message = "Адрес обязателен")
    @Size(max = 255, message = "Адрес не должен превышать 255 символов")
    private String address;

    @Schema(description = "Номер телефона", example = "+7 (495) 123-45-67", required = true)
    @NotBlank(message = "Номер обязателен")
    private String phone;
}