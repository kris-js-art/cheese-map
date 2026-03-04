package com.ryabaya.cheese.mapper;

import com.ryabaya.cheese.dto.request.ProducerRequestDto;
import com.ryabaya.cheese.dto.response.ProducerResponseDto;
import com.ryabaya.cheese.entity.Producer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProducerMapper {

    ProducerResponseDto toResponseDto(Producer producer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheeses", ignore = true)
    Producer toEntity(ProducerRequestDto producerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cheeses", ignore = true)
    void updateEntityFromDto(@MappingTarget Producer producer, ProducerRequestDto producerDto);
}
