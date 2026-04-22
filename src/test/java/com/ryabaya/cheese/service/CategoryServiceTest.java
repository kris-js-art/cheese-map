package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.CategoryRequestDto;
import com.ryabaya.cheese.dto.response.CategoryResponseDto;
import com.ryabaya.cheese.entity.Category;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.CategoryMapper;
import com.ryabaya.cheese.repository.CategoryRepository;
import com.ryabaya.cheese.repository.CheeseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CheeseRepository cheeseRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Cheese cheese;
    private Category category;
    private CategoryRequestDto categoryRequestDto;
    private CategoryResponseDto categoryResponseDto;

    @BeforeEach
    void setUp() {
        cheese = new Cheese();
        cheese.setId(1L);
        cheese.setName("Пармезан");
        cheese.setCategories(new HashSet<>());

        category = new Category();
        category.setId(1L);
        category.setName("Твердые сыры");
        category.setDescription("Сыры с низким содержанием влаги");
        category.setCheeses(new HashSet<>());

        categoryRequestDto = new CategoryRequestDto();
        categoryRequestDto.setName("Твердые сыры");
        categoryRequestDto.setDescription("Сыры с низким содержанием влаги");

        categoryResponseDto = new CategoryResponseDto();
        categoryResponseDto.setId(1L);
        categoryResponseDto.setName("Твердые сыры");
        categoryResponseDto.setDescription("Сыры с низким содержанием влаги");
    }

    @Test
    void createCategory_ShouldReturnCategoryResponseDto_WhenCheeseExists() {
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));
        when(categoryMapper.toEntity(categoryRequestDto)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.createCategory(1L, categoryRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Твердые сыры");
        verify(cheeseRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
        assertThat(cheese.getCategories()).contains(category);
        assertThat(category.getCheeses()).contains(cheese);
    }

    @Test
    void createCategory_ShouldThrowException_WhenCheeseNotFound() {
        when(cheeseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(99L, categoryRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cheese not found with id: 99");

        verify(cheeseRepository).findById(99L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_ShouldReturnCategoryResponseDto_WhenCategoryExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id : 99");

        verify(categoryRepository).findById(99L);
    }

    @Test
    void getCategoryByName_ShouldReturnCategoryResponseDto_WhenCategoryExists() {
        when(categoryRepository.findByName("Твердые сыры")).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDto(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.getCategoryByName("Твердые сыры");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Твердые сыры");
        verify(categoryRepository).findByName("Твердые сыры");
    }

    @Test
    void getCategoryByName_ShouldThrowException_WhenCategoryNotFound() {
        when(categoryRepository.findByName("Несуществующая")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryByName("Несуществующая"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id : Несуществующая");

        verify(categoryRepository).findByName("Несуществующая");
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories() {
        List<Category> categories = List.of(category);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponseDto(category)).thenReturn(categoryResponseDto);

        List<CategoryResponseDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll();
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategoryResponseDto_WhenCategoryExists() {
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Старая категория");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        doAnswer(invocation -> {
            existingCategory.setName(categoryRequestDto.getName());
            existingCategory.setDescription(categoryRequestDto.getDescription());
            return null;
        }).when(categoryMapper).updateEntityFromDto(existingCategory, categoryRequestDto);
        when(categoryRepository.save(existingCategory)).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(categoryResponseDto);

        CategoryResponseDto result = categoryService.updateCategory(1L, categoryRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Твердые сыры");
        verify(categoryRepository).findById(1L);
        verify(categoryMapper).updateEntityFromDto(existingCategory, categoryRequestDto);
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    void updateCategory_ShouldThrowException_WhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, categoryRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id : 99");

        verify(categoryRepository).findById(99L);
        verify(categoryMapper, never()).updateEntityFromDto(any(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_ShouldRemoveCategory_WhenCategoryExists() {
        Category categoryToDelete = new Category();
        categoryToDelete.setId(1L);
        categoryToDelete.setCheeses(new HashSet<>());
        categoryToDelete.getCheeses().add(cheese);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryToDelete));
        doNothing().when(categoryRepository).delete(categoryToDelete);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(categoryToDelete);
        assertThat(cheese.getCategories()).doesNotContain(categoryToDelete);
        assertThat(categoryToDelete.getCheeses()).isEmpty();
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id : 99");

        verify(categoryRepository).findById(99L);
        verify(categoryRepository, never()).delete(any());
    }
}