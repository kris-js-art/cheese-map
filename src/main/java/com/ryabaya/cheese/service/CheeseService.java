package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.CheeseDto;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.mapper.CheeseMapper;
import com.ryabaya.cheese.repository.InMemoryCheeseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheeseService {

    private final InMemoryCheeseRepository cheeseRepository;
    private final CheeseMapper cheeseMapper;

    public CheeseDto getCheeseById(Long id) {
        Cheese cheese = cheeseRepository.findById(id);
        return cheeseMapper.toDto(cheese);
    }

    public CheeseDto getCheeseByName(String name) {
        Cheese cheese = cheeseRepository.findByName(name);
        return cheeseMapper.toDto(cheese);
    }
}
