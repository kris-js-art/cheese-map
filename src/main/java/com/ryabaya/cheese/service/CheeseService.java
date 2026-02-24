package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.CheeseDto;
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
        return cheeseMapper.toDto(cheeseRepository.findById(id)); // маппер нам сделает NoteDto из Note которая придет с репозитория
    }

    public CheeseDto getCheeseByName(String name) {
        return cheeseMapper.toDto(cheeseRepository.findByName(name));
    }
}
