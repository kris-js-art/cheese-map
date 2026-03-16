package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.ShopRequestDto;
import com.ryabaya.cheese.dto.response.ShopResponseDto;
import com.ryabaya.cheese.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Магазины", description = "Управление магазинами сыра")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @Operation(summary = "Создать новый магазин")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Магазин успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    public ResponseEntity<ShopResponseDto> createShop(@Valid @RequestBody ShopRequestDto shopDto) {
        return new ResponseEntity<>(shopService.createShop(shopDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить магазин по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Магазин найден"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден")
    })
    public ResponseEntity<ShopResponseDto> getShopById(
            @Parameter(description = "ID магазина", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping
    @Operation(summary = "Получить все магазины с пагинацией")
    @ApiResponse(responseCode = "200", description = "Список магазинов получен")
    public ResponseEntity<Page<ShopResponseDto>> getAllShops(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (true - возрастание, false - убывание)")
            @RequestParam(defaultValue = "true") boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(shopService.getAllShops(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить магазин")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Магазин обновлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден")
    })
    public ResponseEntity<ShopResponseDto> updateShop(
            @Parameter(description = "ID магазина", required = true) @PathVariable Long id,
            @Valid @RequestBody ShopRequestDto shopDto) {
        return ResponseEntity.ok(shopService.updateShop(id, shopDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить магазин")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Магазин удален"),
            @ApiResponse(responseCode = "404", description = "Магазин не найден")
    })
    public ResponseEntity<Void> deleteShop(
            @Parameter(description = "ID магазина", required = true) @PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}