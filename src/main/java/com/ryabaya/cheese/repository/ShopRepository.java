package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}