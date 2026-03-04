package com.ryabaya.cheese.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CheeseResponseDto {
    private Long id;
    private String name;
    private Double fats;
    private String description;
    private Double price;
    private ProducerResponseDto producer;
    private Set<CategoryResponseDto> categories;
    private List<ReviewResponseDto> reviews;
}
