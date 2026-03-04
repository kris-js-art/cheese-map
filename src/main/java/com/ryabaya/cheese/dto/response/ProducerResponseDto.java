package com.ryabaya.cheese.dto.response;

import lombok.Data;

@Data
public class ProducerResponseDto {
    private Long id;
    private String name;
    private String country;
    private String description;
}
