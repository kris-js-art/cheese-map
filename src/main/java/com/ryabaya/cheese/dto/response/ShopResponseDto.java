package com.ryabaya.cheese.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с данными магазина")
public class ShopResponseDto {

    @Schema(description = "ID магазина", example = "1")
    private Long id;

    @Schema(description = "Название магазина", example = "Сырная лавка")
    private String name;

    @Schema(description = "Адрес магазина", example = "ул. Пушкина, д. 10")
    private String address;

    @Schema(description = "Номер телефона", example = "+7 (495) 123-45-67")
    private String phone;
}