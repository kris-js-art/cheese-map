package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.ProducerRequestDto;
import com.ryabaya.cheese.dto.response.ProducerResponseDto;
import com.ryabaya.cheese.service.ProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Производители", description = "Управление производителями сыра")
public class ProducerController {

    private final ProducerService producerService;

    @PostMapping
    @Operation(summary = "Создать нового производителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Производитель успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    public ResponseEntity<ProducerResponseDto> createProducer(@Valid @RequestBody ProducerRequestDto producerDto) {
        return new ResponseEntity<>(producerService.createProducer(producerDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить производителя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Производитель найден"),
            @ApiResponse(responseCode = "404", description = "Производитель не найден")
    })
    public ResponseEntity<ProducerResponseDto> getProducerById(
            @Parameter(description = "ID производителя", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(producerService.getProducerById(id));
    }

    @GetMapping
    @Operation(summary = "Получить всех производителей")
    @ApiResponse(responseCode = "200", description = "Список производителей получен")
    public ResponseEntity<List<ProducerResponseDto>> getAllProducers() {
        return ResponseEntity.ok(producerService.getAllProducers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить производителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Производитель обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Производитель не найден")
    })
    public ResponseEntity<ProducerResponseDto> updateProducer(
            @Parameter(description = "ID производителя", required = true) @PathVariable Long id,
            @Valid @RequestBody ProducerRequestDto producerDto) {
        return ResponseEntity.ok(producerService.updateProducer(id, producerDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить производителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Производитель удален"),
            @ApiResponse(responseCode = "404", description = "Производитель не найден")
    })
    public ResponseEntity<Void> deleteProducer(
            @Parameter(description = "ID производителя", required = true) @PathVariable Long id) {
        producerService.deleteProducer(id);
        return ResponseEntity.noContent().build();
    }
}