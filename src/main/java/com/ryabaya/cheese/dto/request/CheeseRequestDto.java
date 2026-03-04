package com.ryabaya.cheese.dto.request;

import lombok.Data;

@Data
public class CheeseRequestDto {
    private String name;
    private Double fats;
    private String description;
    private Double price;
}
