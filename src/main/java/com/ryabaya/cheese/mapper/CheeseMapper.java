package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.CheeseDto;
import com.ryabaya.cheese.entity.Cheese;
import org.mapstruct.Mapper;
//маппер нужен для перевода из сущности(Cheese) в DTO(CheeseDto)
@Mapper(componentModel = "spring")
public interface CheeseMapper {

    CheeseDto toDto(Cheese cheese);// мы хотим полуить CheeseDto -> назовем метод toDto -> передавая Cheese
}
