package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.ShopRequestDto;
import com.ryabaya.cheese.dto.response.ShopResponseDto;
import com.ryabaya.cheese.entity.Shop;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.ShopMapper;
import com.ryabaya.cheese.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopMapper shopMapper;

    @InjectMocks
    private ShopService shopService;

    private Shop shop;
    private ShopRequestDto shopRequestDto;
    private ShopResponseDto shopResponseDto;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Сырная лавка");
        shop.setAddress("ул. Пушкина, д. 10");
        shop.setPhone("+7 (495) 123-45-67");

        shopRequestDto = new ShopRequestDto();
        shopRequestDto.setName("Сырная лавка");
        shopRequestDto.setAddress("ул. Пушкина, д. 10");
        shopRequestDto.setPhone("+7 (495) 123-45-67");

        shopResponseDto = new ShopResponseDto();
        shopResponseDto.setId(1L);
        shopResponseDto.setName("Сырная лавка");
        shopResponseDto.setAddress("ул. Пушкина, д. 10");
        shopResponseDto.setPhone("+7 (495) 123-45-67");
    }

    @Test
    void createShop_ShouldReturnShopResponseDto() {
        when(shopMapper.toEntity(shopRequestDto)).thenReturn(shop);
        when(shopRepository.save(any(Shop.class))).thenReturn(shop);
        when(shopMapper.toResponseDto(shop)).thenReturn(shopResponseDto);

        ShopResponseDto result = shopService.createShop(shopRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Сырная лавка");
        verify(shopRepository).save(any(Shop.class));
    }

    @Test
    void getShopById_ShouldReturnShopResponseDto_WhenShopExists() {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(shopMapper.toResponseDto(shop)).thenReturn(shopResponseDto);

        ShopResponseDto result = shopService.getShopById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(shopRepository).findById(1L);
    }

    @Test
    void getShopById_ShouldThrowException_WhenShopNotFound() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.getShopById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shop not found with id: 99");

        verify(shopRepository).findById(99L);
    }

    @Test
    void getAllShops_ShouldReturnPageOfShops() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Shop> shopPage = new PageImpl<>(List.of(shop));

        when(shopRepository.findAll(pageable)).thenReturn(shopPage);
        when(shopMapper.toResponseDto(any(Shop.class))).thenReturn(shopResponseDto);

        Page<ShopResponseDto> result = shopService.getAllShops(pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(shopRepository).findAll(pageable);
    }

    @Test
    void updateShop_ShouldReturnUpdatedShopResponseDto_WhenShopExists() {
        Shop existingShop = new Shop();
        existingShop.setId(1L);
        existingShop.setName("Старый магазин");

        when(shopRepository.findById(1L)).thenReturn(Optional.of(existingShop));
        doAnswer(invocation -> {
            existingShop.setName(shopRequestDto.getName());
            existingShop.setAddress(shopRequestDto.getAddress());
            existingShop.setPhone(shopRequestDto.getPhone());
            return null;
        }).when(shopMapper).updateEntityFromDto(existingShop, shopRequestDto);
        when(shopRepository.save(existingShop)).thenReturn(shop);
        when(shopMapper.toResponseDto(shop)).thenReturn(shopResponseDto);

        ShopResponseDto result = shopService.updateShop(1L, shopRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Сырная лавка");
        verify(shopRepository).findById(1L);
        verify(shopMapper).updateEntityFromDto(existingShop, shopRequestDto);
        verify(shopRepository).save(existingShop);
    }

    @Test
    void updateShop_ShouldThrowException_WhenShopNotFound() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shopService.updateShop(99L, shopRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shop not found with id: 99");

        verify(shopRepository).findById(99L);
        verify(shopMapper, never()).updateEntityFromDto(any(), any());
        verify(shopRepository, never()).save(any());
    }

    @Test
    void deleteShop_ShouldCallDelete_WhenShopExists() {
        when(shopRepository.existsById(1L)).thenReturn(true);
        doNothing().when(shopRepository).deleteById(1L);

        shopService.deleteShop(1L);

        verify(shopRepository).existsById(1L);
        verify(shopRepository).deleteById(1L);
    }

    @Test
    void deleteShop_ShouldThrowException_WhenShopNotFound() {
        when(shopRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> shopService.deleteShop(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shop not found with id: 99");

        verify(shopRepository).existsById(99L);
        verify(shopRepository, never()).deleteById(any());
    }
}