package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.CheeseDto;
import com.ryabaya.cheese.entity.Cheese;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CheeseMapper {

    CheeseDto toDto(Cheese cheese);
}
