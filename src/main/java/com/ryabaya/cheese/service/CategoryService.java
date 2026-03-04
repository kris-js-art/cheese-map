package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.CategoryRequestDto;
import com.ryabaya.cheese.dto.response.CategoryResponseDto;
import com.ryabaya.cheese.entity.Category;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.CategoryMapper;
import com.ryabaya.cheese.repository.CategoryRepository;
import com.ryabaya.cheese.repository.CheeseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND = "Category not found with id : ";

    private final CategoryRepository categoryRepository;
    private final CheeseRepository cheeseRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponseDto createCategory(Long cheeseId, CategoryRequestDto categoryDto) {
        Cheese cheese = cheeseRepository.findById(cheeseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cheese not found with id: " + cheeseId));

        Category category = categoryMapper.toEntity(categoryDto);
        category.getCheeses().add(cheese);
        cheese.getCategories().add(category);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + id));
        return categoryMapper.toResponseDto(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + name));
        return categoryMapper.toResponseDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + id));

        categoryMapper.updateEntityFromDto(category, categoryDto);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + id));

        for (Cheese cheese : category.getCheeses()) {
            cheese.getCategories().remove(category);
        }
        category.getCheeses().clear();
        categoryRepository.delete(category);
    }
}
