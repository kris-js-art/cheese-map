package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.CheeseCreationRequestDto;
import com.ryabaya.cheese.dto.request.CheeseRequestDto;
import com.ryabaya.cheese.dto.response.CheeseResponseDto;
import com.ryabaya.cheese.entity.Category;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.entity.Producer;
import com.ryabaya.cheese.entity.Review;
import com.ryabaya.cheese.entity.Shop;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.CheeseMapper;
import com.ryabaya.cheese.repository.CategoryRepository;
import com.ryabaya.cheese.repository.CheeseRepository;
import com.ryabaya.cheese.repository.ProducerRepository;
import com.ryabaya.cheese.repository.ReviewRepository;
import com.ryabaya.cheese.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheeseService {

    private final CheeseRepository cheeseRepository;
    private final ProducerRepository producerRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final CheeseMapper cheeseMapper;
    private final ReviewRepository reviewRepository;

    @Transactional
    public CheeseResponseDto createCheese(Long shopId, Long producerId, CheeseRequestDto cheeseDto) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        Producer producer = producerRepository.findById(producerId)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + producerId));

        Cheese cheese = cheeseMapper.toEntity(cheeseDto);
        cheese.setShop(shop);
        cheese.setProducer(producer);
        shop.getCheeses().add(cheese);
        producer.getCheeses().add(cheese);
        Cheese savedCheese = cheeseRepository.save(cheese);
        return cheeseMapper.toResponseDto(savedCheese);
    }

    @Transactional(readOnly = true)
    public CheeseResponseDto getCheeseById(Long id) {
        Cheese cheese = cheeseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cheese not found with id: " + id));
        return cheeseMapper.toResponseDto(cheese);
    }

    @Transactional(readOnly = true)
    public CheeseResponseDto getCheeseByName(String name) {
        Cheese cheese = cheeseRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Cheese not found with name: " + name));
        return cheeseMapper.toResponseDto(cheese);
    }

    @Transactional(readOnly = true)
    public List<CheeseResponseDto> getAllCheeses() {
        return cheeseRepository.findAll().stream()
                .map(cheeseMapper::toResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<CheeseResponseDto> getAllCheesesWithGraph() {
        return cheeseRepository.findAllWithGraph().stream()
                .map(cheeseMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CheeseResponseDto updateCheese(Long id, CheeseRequestDto cheeseDto) {
        Cheese cheese = cheeseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cheese not found with id: " + id));

        cheeseMapper.updateEntityFromDto(cheese, cheeseDto);
        Cheese updatedCheese = cheeseRepository.save(cheese);
        return cheeseMapper.toResponseDto(updatedCheese);
    }

    @Transactional
    public void deleteCheese(Long id) {
        if (!cheeseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cheese not found with id: " + id);
        }
        cheeseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CheeseResponseDto> findCheesesByProducer(Long producerId) {
        return cheeseRepository.findByProducerId(producerId).stream()
                .map(cheeseMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CheeseResponseDto createCheeseWithTransaction(
            Long shopId,
            Long producerId,
            CheeseCreationRequestDto request) {
        return cheeseMapper.toResponseDto(createCheeseInternal(shopId, producerId, request));
    }

    public CheeseResponseDto createCheeseWithoutTransaction(
            Long shopId,
            Long producerId,
            CheeseCreationRequestDto request) {
        return cheeseMapper.toResponseDto(createCheeseInternal(shopId, producerId, request));
    }

    public Cheese createCheeseInternal(Long shopId, Long producerId, CheeseCreationRequestDto request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        Producer producer = producerRepository.findById(producerId)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + producerId));

        Cheese cheese = new Cheese();
        cheese.setName(request.getName());
        cheese.setDescription(request.getDescription());
        cheese.setFats(request.getFats());
        cheese.setPrice(request.getPrice());
        cheese.setProducer(producer);
        cheese.setShop(shop);

        Cheese savedCheese = cheeseRepository.save(cheese);

        Category category = new Category();
        category.setName(request.getCategoryName());
        category.setDescription(request.getCategoryDescription());
        Category savedCategory = categoryRepository.save(category);

        savedCheese.getCategories().add(savedCategory);
        savedCategory.getCheeses().add(savedCheese);

        cheeseRepository.save(savedCheese);
        categoryRepository.save(savedCategory);

        if (request.isInitiatedProblem()) {
            throw new ResourceNotFoundException("Initiated problem was called");
        }

        Review review = new Review();
        review.setAuthor(request.getReviewAuthor());
        review.setRating(request.getReviewRating());
        review.setComment(request.getReviewComment());
        review.setCheese(savedCheese);

        Review savedReview = reviewRepository.save(review);

        savedCheese.getReviews().add(savedReview);

        return savedCheese;
    }


}
