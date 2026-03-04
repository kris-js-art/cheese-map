package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.ProducerRequestDto;
import com.ryabaya.cheese.dto.response.ProducerResponseDto;
import com.ryabaya.cheese.service.ProducerService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/producers")
@RequiredArgsConstructor
public class ProducerController {

    private final ProducerService producerService;

    @PostMapping
    public ResponseEntity<ProducerResponseDto> createProducer(@RequestBody ProducerRequestDto producerDto) {
        return new ResponseEntity<>(producerService.createProducer(producerDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProducerResponseDto> getProducerById(@PathVariable Long id) {
        return ResponseEntity.ok(producerService.getProducerById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProducerResponseDto>> getAllProducers() {
        return ResponseEntity.ok(producerService.getAllProducers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProducerResponseDto> updateProducer(
            @PathVariable Long id,
            @RequestBody ProducerRequestDto producerDto) {
        return ResponseEntity.ok(producerService.updateProducer(id, producerDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducer(@PathVariable Long id) {
        producerService.deleteProducer(id);
        return ResponseEntity.noContent().build();
    }
}
