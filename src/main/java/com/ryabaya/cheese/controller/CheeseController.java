package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.CheeseDto;
import com.ryabaya.cheese.service.CheeseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor//аннотация которая автоматически создаст конструктор
public class CheeseController {

    private final CheeseService cheeseService;

    @GetMapping("/{id}") // с помощтю этой аннотации мы указываем по какому адресу можно что-то получить
    public ResponseEntity<CheeseDto> getCheeseById(@PathVariable Long id) { // с помощью аннотации PathVariable получить переменную из адреса
        return ResponseEntity.ok(cheeseService.getCheeseById(id));// возвращаем ответ
    }

    @GetMapping("/name")
    public ResponseEntity<CheeseDto> getCheeseByName(@RequestParam String name) { // c помощтю rq мы читаем параметр(переменная после вопроса)
        return ResponseEntity.ok(cheeseService.getCheeseByName(name));
    }
}
