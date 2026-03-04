package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Producer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducerRepository extends JpaRepository<Producer, Long> {
}