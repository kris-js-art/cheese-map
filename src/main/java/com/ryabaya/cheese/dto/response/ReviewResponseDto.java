package com.ryabaya.cheese.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {
    private Long id;
    private String author;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
