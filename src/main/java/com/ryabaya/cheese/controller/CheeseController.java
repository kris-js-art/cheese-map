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
@RequiredArgsConstructor
public class CheeseController {

    private final CheeseService cheeseService;

    @GetMapping("/{id}")
    public ResponseEntity<CheeseDto> getCheeseById(@PathVariable Long id) {
        return ResponseEntity.ok(cheeseService.getCheeseById(id));
    }

    @GetMapping("/name")
    public ResponseEntity<CheeseDto> getCheeseByName(@RequestParam String name) {
        return ResponseEntity.ok(cheeseService.getCheeseByName(name));
    }
}
