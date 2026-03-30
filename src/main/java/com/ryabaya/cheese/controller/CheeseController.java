package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.CheeseBulkRequestDto;
import com.ryabaya.cheese.dto.request.CheeseCreationRequestDto;
import com.ryabaya.cheese.dto.request.CheeseRequestDto;
import com.ryabaya.cheese.dto.response.CheeseResponseDto;
import com.ryabaya.cheese.service.CheeseService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cheeses")
@RequiredArgsConstructor
@Tag(name = "Сыры", description = "Управление сырами")
public class CheeseController {

    private final CheeseService cheeseService;

    @PostMapping("/{shopId}/{producerId}")
    @Operation(summary = "Создать новый сыр", description = "Создает сыр и привязывает к магазину и производителю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сыр успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Магазин или производитель не найдены")
    })
    public ResponseEntity<CheeseResponseDto> createCheese(
            @Parameter(description = "ID магазина", required = true) @PathVariable Long shopId,
            @Parameter(description = "ID производителя", required = true) @PathVariable Long producerId,
            @Valid @RequestBody CheeseRequestDto cheeseDto) {
        return new ResponseEntity<>(cheeseService.createCheese(shopId, producerId, cheeseDto), HttpStatus.CREATED);
    }

    @PostMapping("/{shopId}/{producerId}/withoutTransaction")
    @Operation(summary = "Создать сыр без транзакции",
            description = "Создает сыр с категорией и отзывом без транзакции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сыр успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    })
    public ResponseEntity<CheeseResponseDto> createCheeseWithoutTransaction(
            @Parameter(description = "ID магазина", required = true) @PathVariable Long shopId,
            @Parameter(description = "ID производителя", required = true) @PathVariable Long producerId,
            @Valid @RequestBody CheeseCreationRequestDto cheeseDto) {
        return new ResponseEntity<>(
                cheeseService.createCheeseWithoutTransaction(shopId, producerId, cheeseDto), HttpStatus.CREATED
        );
    }

    @PostMapping("/{shopId}/{producerId}/withTransaction")
    @Operation(summary = "Создать сыр с транзакцией",
            description = "Создает сыр с категорией и отзывом в рамках транзакции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сыр успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    })
    public ResponseEntity<CheeseResponseDto> createCheeseWithTransaction(
            @Parameter(description = "ID магазина", required = true) @PathVariable Long shopId,
            @Parameter(description = "ID производителя", required = true) @PathVariable Long producerId,
            @Valid @RequestBody CheeseCreationRequestDto cheeseDto) {
        return new ResponseEntity<>(
                cheeseService.createCheeseWithTransaction(shopId, producerId, cheeseDto), HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить сыр по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сыр найден"),
            @ApiResponse(responseCode = "404", description = "Сыр не найден")
    })
    public ResponseEntity<CheeseResponseDto> getCheeseById(
            @Parameter(description = "ID сыра", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(cheeseService.getCheeseById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск сыра по названию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сыр найден"),
            @ApiResponse(responseCode = "404", description = "Сыр не найден")
    })
    public ResponseEntity<CheeseResponseDto> getCheeseByName(
            @Parameter(description = "Название сыра", required = true) @RequestParam String name) {
        return ResponseEntity.ok(cheeseService.getCheeseByName(name));
    }

    @GetMapping
    @Operation(summary = "Получить все сыры")
    @ApiResponse(responseCode = "200", description = "Список сыров получен")
    public ResponseEntity<List<CheeseResponseDto>> getAllCheeses() {
        return ResponseEntity.ok(cheeseService.getAllCheeses());
    }

    @GetMapping("/graph")
    @Operation(summary = "Получить все сыры с графом сущностей",
            description = "Загружает сыры со связанными сущностями")
    @ApiResponse(responseCode = "200", description = "Список сыров получен")
    public ResponseEntity<List<CheeseResponseDto>> getAllCheesesWithGraph() {
        return ResponseEntity.ok(cheeseService.getAllCheesesWithGraph());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить сыр")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сыр обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Сыр не найден")
    })
    public ResponseEntity<CheeseResponseDto> updateCheese(
            @Parameter(description = "ID сыра", required = true) @PathVariable Long id,
            @Valid @RequestBody CheeseRequestDto cheeseDto) {
        return ResponseEntity.ok(cheeseService.updateCheese(id, cheeseDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить сыр")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Сыр удален"),
            @ApiResponse(responseCode = "404", description = "Сыр не найден")
    })
    public ResponseEntity<Void> deleteCheese(
            @Parameter(description = "ID сыра", required = true) @PathVariable Long id) {
        cheeseService.deleteCheese(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter/producer/{producerId}")
    @Operation(summary = "Получить сыры по производителю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список сыров получен"),
            @ApiResponse(responseCode = "404", description = "Производитель не найден")
    })
    public ResponseEntity<List<CheeseResponseDto>> getCheesesByProducer(
            @Parameter(description = "ID производителя", required = true) @PathVariable Long producerId) {
        return ResponseEntity.ok(cheeseService.findCheesesByProducer(producerId));
    }

    @GetMapping("/search/jpql")
    @Operation(summary = "Поиск сыров (JPQL)",
            description = "Поиск по стране производителя, категории и макс. содержанию жира")
    @ApiResponse(responseCode = "200", description = "Результаты поиска")
    public ResponseEntity<List<CheeseResponseDto>> searchCheeses(
            @Parameter(description = "Страна производителя", required = true) @RequestParam String producerCountry,
            @Parameter(description = "Название категории", required = true) @RequestParam String categoryName,
            @Parameter(description = "Максимальное содержание жира", required = true) @RequestParam Double maxFats) {
        List<CheeseResponseDto> cheeses = cheeseService.searchCheesesJpql(producerCountry, categoryName, maxFats);
        return ResponseEntity.ok(cheeses);
    }

    @GetMapping("/search/native")
    @Operation(summary = "Поиск сыров (Native SQL)",
            description = "Поиск по стране производителя, категории и макс. содержанию жира (нативный SQL)")
    @ApiResponse(responseCode = "200", description = "Результаты поиска")
    public ResponseEntity<List<CheeseResponseDto>> searchCheesesNative(
            @Parameter(description = "Страна производителя", required = true) @RequestParam String producerCountry,
            @Parameter(description = "Название категории", required = true) @RequestParam String categoryName,
            @Parameter(description = "Максимальное содержание жира", required = true) @RequestParam Double maxFats) {
        List<CheeseResponseDto> cheeses = cheeseService.searchCheesesNative(producerCountry, categoryName, maxFats);
        return ResponseEntity.ok(cheeses);
    }

    @PostMapping("/{shopId}/{producerId}/bulk/withTx")
    @Operation(summary = "Массовое создание сыров с транзакцией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сыры успешно созданы"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Магазин или производитель не найдены"),
            @ApiResponse(responseCode = "500", description = "Имитация проблемы - часть данных сохранена/не сохранена")
    })
    public ResponseEntity<List<CheeseResponseDto>> bulkCreateCheesesWithTx(
            @PathVariable Long shopId,
            @PathVariable Long producerId,
            @Valid @RequestBody CheeseBulkRequestDto request) {
        return new ResponseEntity<>(
                cheeseService.bulkCreateCheesesWithTx(shopId, producerId, request),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{shopId}/{producerId}/bulk/woTx")
    @Operation(summary = "Массовое создание сыров без транзакции")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Сыры успешно созданы"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Магазин или производитель не найдены"),
            @ApiResponse(responseCode = "500", description = "Имитация проблемы - часть данных сохранена/не сохранена")
    })
    public ResponseEntity<List<CheeseResponseDto>> bulkCreateCheesesWoTx(
            @PathVariable Long shopId,
            @PathVariable Long producerId,
            @Valid @RequestBody CheeseBulkRequestDto request) {
        return new ResponseEntity<>(
                cheeseService.bulkCreateCheesesWoTx(shopId, producerId, request),
                HttpStatus.CREATED
        );
    }

}