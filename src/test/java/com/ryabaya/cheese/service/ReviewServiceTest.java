package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.ReviewRequestDto;
import com.ryabaya.cheese.dto.response.ReviewResponseDto;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.entity.Review;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.ReviewMapper;
import com.ryabaya.cheese.repository.CheeseRepository;
import com.ryabaya.cheese.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CheeseRepository cheeseRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private Cheese cheese;
    private Review review;
    private ReviewRequestDto reviewRequestDto;
    private ReviewResponseDto reviewResponseDto;

    @BeforeEach
    void setUp() {
        cheese = new Cheese();
        cheese.setId(1L);
        cheese.setName("Пармезан");
        cheese.setReviews(new ArrayList<>());

        review = new Review();
        review.setId(1L);
        review.setAuthor("Иван Петров");
        review.setRating(5);
        review.setComment("Отличный сыр!");
        review.setCheese(cheese);

        reviewRequestDto = new ReviewRequestDto();
        reviewRequestDto.setAuthor("Иван Петров");
        reviewRequestDto.setRating(5);
        reviewRequestDto.setComment("Отличный сыр!");

        reviewResponseDto = new ReviewResponseDto();
        reviewResponseDto.setId(1L);
        reviewResponseDto.setAuthor("Иван Петров");
        reviewResponseDto.setRating(5);
        reviewResponseDto.setComment("Отличный сыр!");
    }

    @Test
    void createReview_ShouldReturnReviewResponseDto_WhenCheeseExists() {
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));
        when(reviewMapper.toEntity(reviewRequestDto)).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponseDto(review)).thenReturn(reviewResponseDto);

        ReviewResponseDto result = reviewService.createReview(1L, reviewRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAuthor()).isEqualTo("Иван Петров");
        verify(cheeseRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
        assertThat(cheese.getReviews()).contains(review);
    }

    @Test
    void createReview_ShouldThrowException_WhenCheeseNotFound() {
        when(cheeseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(99L, reviewRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cheese not found with id: 99");

        verify(cheeseRepository).findById(99L);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewById_ShouldReturnReviewResponseDto_WhenReviewExists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewMapper.toResponseDto(review)).thenReturn(reviewResponseDto);

        ReviewResponseDto result = reviewService.getReviewById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReviewById_ShouldThrowException_WhenReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found with id: 99");

        verify(reviewRepository).findById(99L);
    }

    @Test
    void getReviewsByCheeseId_ShouldReturnListOfReviews() {
        List<Review> reviews = List.of(review);
        when(reviewRepository.findByCheeseId(1L)).thenReturn(reviews);
        when(reviewMapper.toResponseDto(review)).thenReturn(reviewResponseDto);

        List<ReviewResponseDto> result = reviewService.getReviewsByCheeseId(1L);

        assertThat(result).hasSize(1);
        verify(reviewRepository).findByCheeseId(1L);
    }

    @Test
    void updateReview_ShouldReturnUpdatedReviewResponseDto_WhenReviewExists() {
        Review existingReview = new Review();
        existingReview.setId(1L);
        existingReview.setAuthor("Старый автор");
        existingReview.setRating(3);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existingReview));
        doAnswer(invocation -> {
            existingReview.setAuthor(reviewRequestDto.getAuthor());
            existingReview.setRating(reviewRequestDto.getRating());
            existingReview.setComment(reviewRequestDto.getComment());
            return null;
        }).when(reviewMapper).updateEntityFromDto(existingReview, reviewRequestDto);
        when(reviewRepository.save(existingReview)).thenReturn(review);
        when(reviewMapper.toResponseDto(review)).thenReturn(reviewResponseDto);

        ReviewResponseDto result = reviewService.updateReview(1L, reviewRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getAuthor()).isEqualTo("Иван Петров");
        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).findById(1L);
        verify(reviewMapper).updateEntityFromDto(existingReview, reviewRequestDto);
        verify(reviewRepository).save(existingReview);
    }

    @Test
    void updateReview_ShouldThrowException_WhenReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(99L, reviewRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found with id: 99");

        verify(reviewRepository).findById(99L);
        verify(reviewMapper, never()).updateEntityFromDto(any(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_ShouldRemoveReview_WhenReviewExists() {
        Review reviewToDelete = new Review();
        reviewToDelete.setId(1L);
        reviewToDelete.setCheese(cheese);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewToDelete));
        doNothing().when(reviewRepository).delete(reviewToDelete);

        reviewService.deleteReview(1L);

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(reviewToDelete);
        assertThat(cheese.getReviews()).doesNotContain(reviewToDelete);
    }

    @Test
    void deleteReview_ShouldThrowException_WhenReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found with id: 99");

        verify(reviewRepository).findById(99L);
        verify(reviewRepository, never()).delete(any());
    }
}