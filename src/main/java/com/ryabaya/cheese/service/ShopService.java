package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.ShopRequestDto;
import com.ryabaya.cheese.dto.response.ShopResponseDto;
import com.ryabaya.cheese.entity.Shop;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.ShopMapper;
import com.ryabaya.cheese.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;

    public ShopResponseDto createShop(ShopRequestDto shopDto) {
        Shop shop = shopMapper.toEntity(shopDto);
        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toResponseDto(savedShop);
    }

    @Transactional(readOnly = true)
    public ShopResponseDto getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));
        return shopMapper.toResponseDto(shop);
    }

    @Transactional(readOnly = true)
    public Page<ShopResponseDto> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable)
                .map(shopMapper::toResponseDto);
    }

    public ShopResponseDto updateShop(Long id, ShopRequestDto shopDto) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + id));

        shopMapper.updateEntityFromDto(shop, shopDto);
        Shop updatedShop = shopRepository.save(shop);
        return shopMapper.toResponseDto(updatedShop);
    }

    public void deleteShop(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shop not found with id: " + id);
        }
        shopRepository.deleteById(id);
    }
}
