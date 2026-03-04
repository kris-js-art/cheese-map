package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.request.ShopRequestDto;
import com.ryabaya.cheese.dto.response.ShopResponseDto;
import com.ryabaya.cheese.entity.Shop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShopMapper {

    ShopResponseDto toResponseDto(Shop shop);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheeses", ignore = true)
    Shop toEntity(ShopRequestDto shopDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheeses", ignore = true)
    void updateEntityFromDto(@MappingTarget Shop shop, ShopRequestDto shopDto);
}
