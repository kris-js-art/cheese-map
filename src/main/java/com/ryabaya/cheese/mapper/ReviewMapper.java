package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.request.ReviewRequestDto;
import com.ryabaya.cheese.dto.response.ReviewResponseDto;
import com.ryabaya.cheese.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewResponseDto toResponseDto(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheese", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(ReviewRequestDto reviewDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheese", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(@MappingTarget Review review, ReviewRequestDto reviewDto);
}
