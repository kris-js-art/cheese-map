package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.CheeseDto;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.mapper.CheeseMapper;
import com.ryabaya.cheese.repository.InMemoryCheeseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// пока что выполняет роль посредника между контроллером и репозиторием
public class CheeseService {

    private final InMemoryCheeseRepository cheeseRepository;
    private final CheeseMapper cheeseMapper;

    public CheeseDto getCheeseById(Long id) {
        Cheese cheese = cheeseRepository.findById(id);// получаем сыр из репозитория
        return cheeseMapper.toDto(cheese); // маппер нам сделает CheeseDto из Cheese которая придет с репозитория
    }

    public CheeseDto getCheeseByName(String name) {
        Cheese cheese = cheeseRepository.findByName(name);
        return cheeseMapper.toDto(cheese);
    }
}
