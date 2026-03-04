package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.request.CheeseRequestDto;
import com.ryabaya.cheese.dto.response.CheeseResponseDto;
import com.ryabaya.cheese.entity.Cheese;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ProducerMapper.class,
        ShopMapper.class, CategoryMapper.class, ReviewMapper.class})
public interface CheeseMapper {

    CheeseResponseDto toResponseDto(Cheese cheese);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producer", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Cheese toEntity(CheeseRequestDto cheeseDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producer", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void updateEntityFromDto(@MappingTarget Cheese cheese, CheeseRequestDto cheeseDto);
}
