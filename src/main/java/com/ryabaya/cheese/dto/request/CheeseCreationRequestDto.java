package com.ryabaya.cheese.dto.request;

import lombok.Data;

@Data
public class CheeseCreationRequestDto {

    private String name;
    private Double fats;
    private String description;
    private Double price;

    private String categoryName;
    private String categoryDescription;

    private String reviewAuthor;
    private Integer reviewRating;
    private String reviewComment;

    private boolean initiatedProblem;

}
