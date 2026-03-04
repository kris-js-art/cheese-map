package com.ryabaya.cheese.dto.request;

import lombok.Data;

@Data
public class ReviewRequestDto {
    private String author;
    private Integer rating;
    private String comment;
}
