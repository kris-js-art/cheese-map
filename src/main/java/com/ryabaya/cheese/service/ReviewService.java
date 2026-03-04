package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.ReviewRequestDto;
import com.ryabaya.cheese.dto.response.ReviewResponseDto;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.entity.Review;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.ReviewMapper;
import com.ryabaya.cheese.repository.CheeseRepository;
import com.ryabaya.cheese.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private static final String REVIEW_NOT_FOUND = "Review not found with id: ";

    private final ReviewRepository reviewRepository;
    private final CheeseRepository cheeseRepository;
    private final ReviewMapper reviewMapper;

    public ReviewResponseDto createReview(Long cheeseId, ReviewRequestDto reviewDto) {
        Cheese cheese = cheeseRepository.findById(cheeseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cheese not found with id: " + cheeseId));

        Review review = reviewMapper.toEntity(reviewDto);
        review.setCheese(cheese);
        cheese.getReviews().add(review);

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDto(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND + id));
        return reviewMapper.toResponseDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByCheeseId(Long cheeseId) {
        return reviewRepository.findByCheeseId(cheeseId).stream()
                .map(reviewMapper::toResponseDto)
                .toList();
    }

    public ReviewResponseDto updateReview(Long id, ReviewRequestDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND + id));

        reviewMapper.updateEntityFromDto(review, reviewDto);
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDto(updatedReview);
    }

    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND + id));

        review.getCheese().getReviews().remove(review);
        reviewRepository.delete(review);
    }
}
