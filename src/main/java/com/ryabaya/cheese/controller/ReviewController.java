package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.ReviewRequestDto;
import com.ryabaya.cheese.dto.response.ReviewResponseDto;
import com.ryabaya.cheese.service.ReviewService;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/cheese/{cheeseId}")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long cheeseId,
            @RequestBody ReviewRequestDto reviewDto) {
        return new ResponseEntity<>(reviewService.createReview(cheeseId, reviewDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/cheese/{cheeseId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByCheeseId(@PathVariable Long cheeseId) {
        return ResponseEntity.ok(reviewService.getReviewsByCheeseId(cheeseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long id,
            @RequestBody ReviewRequestDto reviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
