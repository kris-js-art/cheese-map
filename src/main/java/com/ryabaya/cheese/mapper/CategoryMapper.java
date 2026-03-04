package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.request.CategoryRequestDto;
import com.ryabaya.cheese.dto.response.CategoryResponseDto;
import com.ryabaya.cheese.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDto toResponseDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheeses", ignore = true)
    Category toEntity(CategoryRequestDto categoryDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheeses", ignore = true)
    void updateEntityFromDto(@MappingTarget Category category, CategoryRequestDto categoryDto);
}
