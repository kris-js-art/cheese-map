package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.ShopRequestDto;
import com.ryabaya.cheese.dto.response.ShopResponseDto;
import com.ryabaya.cheese.service.ShopService;
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
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ShopResponseDto> createShop(@RequestBody ShopRequestDto shopDto) {
        return new ResponseEntity<>(shopService.createShop(shopDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShopResponseDto>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponseDto> updateShop(@PathVariable Long id, @RequestBody ShopRequestDto shopDto) {
        return ResponseEntity.ok(shopService.updateShop(id, shopDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
