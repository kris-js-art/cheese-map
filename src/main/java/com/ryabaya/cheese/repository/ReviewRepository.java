package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCheeseId(Long cheeseId);
}