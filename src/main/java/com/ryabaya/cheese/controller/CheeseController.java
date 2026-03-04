package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.CheeseCreationRequestDto;
import com.ryabaya.cheese.dto.request.CheeseRequestDto;
import com.ryabaya.cheese.dto.response.CheeseResponseDto;
import com.ryabaya.cheese.service.CheeseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cheeses")
@RequiredArgsConstructor
public class CheeseController {

    private final CheeseService cheeseService;

    @PostMapping("/{shopId}/{producerId}")
    public ResponseEntity<CheeseResponseDto> createCheese(
            @PathVariable Long shopId,
            @PathVariable Long producerId,
            @RequestBody CheeseRequestDto cheeseDto) {
        return new ResponseEntity<>(cheeseService.createCheese(shopId, producerId, cheeseDto), HttpStatus.CREATED);
    }


    @PostMapping("/{shopId}/{producerId}/withoutTransaction")
    public ResponseEntity<CheeseResponseDto> createCheeseWithoutTransaction(
            @PathVariable Long shopId,
            @PathVariable Long producerId,
            @RequestBody CheeseCreationRequestDto cheeseDto) {
        return new ResponseEntity<>(
                cheeseService.createCheeseWithoutTransaction(shopId, producerId, cheeseDto), HttpStatus.CREATED
        );
    }

    @PostMapping("/{shopId}/{producerId}/withTransaction")
    public ResponseEntity<CheeseResponseDto> createCheeseWithTransaction(
            @PathVariable Long shopId,
            @PathVariable Long producerId,
            @RequestBody CheeseCreationRequestDto cheeseDto) {
        return new ResponseEntity<>(
                cheeseService.createCheeseWithTransaction(shopId, producerId, cheeseDto), HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheeseResponseDto> getCheeseById(@PathVariable Long id) {
        return ResponseEntity.ok(cheeseService.getCheeseById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<CheeseResponseDto> getCheeseByName(@RequestParam String name) {
        return ResponseEntity.ok(cheeseService.getCheeseByName(name));
    }

    @GetMapping
    public ResponseEntity<List<CheeseResponseDto>> getAllCheeses() {
        return ResponseEntity.ok(cheeseService.getAllCheeses());
    }

    @GetMapping("/graph")
    public ResponseEntity<List<CheeseResponseDto>> getAllCheesesWithGraph() {
        return ResponseEntity.ok(cheeseService.getAllCheesesWithGraph());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheeseResponseDto> updateCheese(
            @PathVariable Long id,
            @RequestBody CheeseRequestDto cheeseDto) {
        return ResponseEntity.ok(cheeseService.updateCheese(id, cheeseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheese(@PathVariable Long id) {
        cheeseService.deleteCheese(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter/producer/{producerId}")
    public ResponseEntity<List<CheeseResponseDto>> getCheesesByProducer(@PathVariable Long producerId) {
        return ResponseEntity.ok(cheeseService.findCheesesByProducer(producerId));
    }
}
