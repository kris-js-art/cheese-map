package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Page<Shop> findAll(Pageable pageable);

}