package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.CheeseBulkRequestDto;
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
import com.ryabaya.cheese.entity.AsyncTask;
import com.ryabaya.cheese.entity.AsyncTaskStatus;
import com.ryabaya.cheese.repository.CategoryRepository;
import com.ryabaya.cheese.repository.CheeseRepository;
import com.ryabaya.cheese.repository.ProducerRepository;
import com.ryabaya.cheese.repository.ReviewRepository;
import com.ryabaya.cheese.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheeseServiceTest {

    @Mock
    private CheeseRepository cheeseRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ProducerRepository producerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CheeseMapper cheeseMapper;

    @Mock
    private AsyncTaskStorage asyncTaskStorage;

    @Mock
    private AsyncCheeseExecutorService asyncCheeseExecutorService;

    @InjectMocks
    private CheeseService cheeseService;

    private Shop shop;
    private Producer producer;
    private Cheese cheese;
    private CheeseRequestDto cheeseRequestDto;
    private CheeseResponseDto cheeseResponseDto;
    private CheeseCreationRequestDto cheeseCreationRequestDto;
    private CheeseBulkRequestDto cheeseBulkRequestDto;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Сырная лавка");
        shop.setCheeses(new ArrayList<>());

        producer = new Producer();
        producer.setId(1L);
        producer.setName("Parmigiano Reggiano");
        producer.setCheeses(new ArrayList<>());

        cheese = new Cheese();
        cheese.setId(1L);
        cheese.setName("Пармезан");
        cheese.setFats(32.5);
        cheese.setDescription("Итальянский твердый сыр");
        cheese.setPrice(1250.50);
        cheese.setShop(shop);
        cheese.setProducer(producer);
        cheese.setCategories(new HashSet<>());
        cheese.setReviews(new ArrayList<>());

        cheeseRequestDto = new CheeseRequestDto();
        cheeseRequestDto.setName("Пармезан");
        cheeseRequestDto.setFats(32.5);
        cheeseRequestDto.setDescription("Итальянский твердый сыр");
        cheeseRequestDto.setPrice(1250.50);

        cheeseResponseDto = new CheeseResponseDto();
        cheeseResponseDto.setId(1L);
        cheeseResponseDto.setName("Пармезан");
        cheeseResponseDto.setFats(32.5);
        cheeseResponseDto.setDescription("Итальянский твердый сыр");
        cheeseResponseDto.setPrice(1250.50);

        cheeseCreationRequestDto = new CheeseCreationRequestDto();
        cheeseCreationRequestDto.setName("Пармезан");
        cheeseCreationRequestDto.setFats(32.5);
        cheeseCreationRequestDto.setDescription("Итальянский твердый сыр");
        cheeseCreationRequestDto.setPrice(1250.50);
        cheeseCreationRequestDto.setCategoryName("Твердые сыры");
        cheeseCreationRequestDto.setCategoryDescription("Описание категории");
        cheeseCreationRequestDto.setReviewAuthor("Иван");
        cheeseCreationRequestDto.setReviewRating(5);
        cheeseCreationRequestDto.setReviewComment("Отличный сыр");
        cheeseCreationRequestDto.setInitiatedProblem(false);

        List<CheeseRequestDto> requests = new ArrayList<>();
        requests.add(cheeseRequestDto);
        requests.add(cheeseRequestDto);

        cheeseBulkRequestDto = new CheeseBulkRequestDto();
        cheeseBulkRequestDto.setCheeses(requests);
        cheeseBulkRequestDto.setInitiatedProblem(false);
    }

    @Test
    void createCheese_ShouldReturnCheeseResponseDto_WhenValidInput() {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(cheeseMapper.toEntity(cheeseRequestDto)).thenReturn(cheese);
        when(cheeseRepository.save(any(Cheese.class))).thenReturn(cheese);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        CheeseResponseDto result = cheeseService.createCheese(1L, 1L, cheeseRequestDto);

        assertThat(result).isNotNull();
        verify(shopRepository).findById(1L);
        verify(producerRepository).findById(1L);
        verify(cheeseRepository).save(any(Cheese.class));
    }

    @Test
    void createCheese_ShouldThrowException_WhenShopNotFound() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.createCheese(99L, 1L, cheeseRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Shop not found with id: 99");

        verify(shopRepository).findById(99L);
        verify(producerRepository, never()).findById(any());
        verify(cheeseRepository, never()).save(any());
    }

    @Test
    void createCheese_ShouldThrowException_WhenProducerNotFound() {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.createCheese(1L, 99L, cheeseRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producer not found with id: 99");

        verify(shopRepository).findById(1L);
        verify(producerRepository).findById(99L);
        verify(cheeseRepository, never()).save(any());
    }

    @Test
    void getCheeseById_ShouldReturnCheeseResponseDto_WhenCheeseExists() {
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        CheeseResponseDto result = cheeseService.getCheeseById(1L);

        assertThat(result).isNotNull();
        verify(cheeseRepository).findById(1L);
    }

    @Test
    void getCheeseById_ShouldThrowException_WhenCheeseNotFound() {
        when(cheeseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.getCheeseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cheese not found with id: 99");

        verify(cheeseRepository).findById(99L);
    }

    @Test
    void getCheeseByName_ShouldReturnCheeseResponseDto_WhenCheeseExists() {
        when(cheeseRepository.findByName("Пармезан")).thenReturn(Optional.of(cheese));
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        CheeseResponseDto result = cheeseService.getCheeseByName("Пармезан");

        assertThat(result).isNotNull();
        verify(cheeseRepository).findByName("Пармезан");
    }

    @Test
    void getCheeseByName_ShouldThrowException_WhenCheeseNotFound() {
        when(cheeseRepository.findByName("Несуществующий")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.getCheeseByName("Несуществующий"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cheese not found with name: Несуществующий");

        verify(cheeseRepository).findByName("Несуществующий");
    }

    @Test
    void getAllCheeses_ShouldReturnListOfCheeses() {
        List<Cheese> cheeses = List.of(cheese);
        when(cheeseRepository.findAll()).thenReturn(cheeses);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.getAllCheeses();

        assertThat(result).hasSize(1);
        verify(cheeseRepository).findAll();
    }

    @Test
    void getAllCheesesWithGraph_ShouldReturnListOfCheeses() {
        List<Cheese> cheeses = List.of(cheese);
        when(cheeseRepository.findAllWithGraph()).thenReturn(cheeses);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.getAllCheesesWithGraph();

        assertThat(result).hasSize(1);
        verify(cheeseRepository).findAllWithGraph();
    }

    @Test
    void updateCheese_ShouldReturnUpdatedCheeseResponseDto_WhenCheeseExists() {
        when(cheeseRepository.findById(1L)).thenReturn(Optional.of(cheese));
        doNothing().when(cheeseMapper).updateEntityFromDto(cheese, cheeseRequestDto);
        when(cheeseRepository.save(cheese)).thenReturn(cheese);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        CheeseResponseDto result = cheeseService.updateCheese(1L, cheeseRequestDto);

        assertThat(result).isNotNull();
        verify(cheeseRepository).findById(1L);
        verify(cheeseMapper).updateEntityFromDto(cheese, cheeseRequestDto);
        verify(cheeseRepository).save(cheese);
    }

    @Test
    void updateCheese_ShouldThrowException_WhenCheeseNotFound() {
        when(cheeseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.updateCheese(99L, cheeseRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cheese not found with id: 99");

        verify(cheeseRepository).findById(99L);
        verify(cheeseMapper, never()).updateEntityFromDto(any(), any());
        verify(cheeseRepository, never()).save(any());
    }

    @Test
    void deleteCheese_ShouldCallDelete_WhenCheeseExists() {
        when(cheeseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cheeseRepository).deleteById(1L);

        cheeseService.deleteCheese(1L);

        verify(cheeseRepository).existsById(1L);
        verify(cheeseRepository).deleteById(1L);
    }

    @Test
    void deleteCheese_ShouldThrowException_WhenCheeseNotFound() {
        when(cheeseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> cheeseService.deleteCheese(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cheese not found with id: 99");

        verify(cheeseRepository).existsById(99L);
        verify(cheeseRepository, never()).deleteById(any());
    }

    @Test
    void findCheesesByProducer_ShouldReturnListOfCheeses() {
        List<Cheese> cheeses = List.of(cheese);
        when(cheeseRepository.findByProducerId(1L)).thenReturn(cheeses);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.findCheesesByProducer(1L);

        assertThat(result).hasSize(1);
        verify(cheeseRepository).findByProducerId(1L);
    }

    @Test
    void createCheeseWithTransaction_ShouldReturnCheeseResponseDto() {
        Category category = new Category();
        category.setId(1L);
        category.setCheeses(new HashSet<>());

        Review review = new Review();
        review.setId(1L);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(cheeseRepository.save(any(Cheese.class))).thenReturn(cheese);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(cheeseMapper.toResponseDto(any(Cheese.class))).thenReturn(cheeseResponseDto);

        CheeseResponseDto result = cheeseService.createCheeseWithTransaction(1L, 1L, cheeseCreationRequestDto);

        assertThat(result).isNotNull();
        verify(cheeseRepository, times(2)).save(any(Cheese.class));
        verify(categoryRepository, times(2)).save(any(Category.class));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createCheeseWithoutTransaction_ShouldReturnCheeseResponseDto() {
        Category category = new Category();
        category.setId(1L);
        category.setCheeses(new HashSet<>());

        Review review = new Review();
        review.setId(1L);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(cheeseRepository.save(any(Cheese.class))).thenReturn(cheese);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(cheeseMapper.toResponseDto(any(Cheese.class))).thenReturn(cheeseResponseDto);

        CheeseResponseDto result = cheeseService.createCheeseWithoutTransaction(1L, 1L, cheeseCreationRequestDto);

        assertThat(result).isNotNull();
        verify(cheeseRepository, times(2)).save(any(Cheese.class));
        verify(categoryRepository, times(2)).save(any(Category.class));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createCheeseInternal_ShouldThrowException_WhenInitiatedProblemTrue() {
        cheeseCreationRequestDto.setInitiatedProblem(true);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(cheeseRepository.save(any(Cheese.class))).thenReturn(cheese);
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

        assertThatThrownBy(() -> cheeseService.createCheeseInternal(1L, 1L, cheeseCreationRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Initiated problem was called");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void searchCheesesJpql_ShouldReturnListOfCheeses() {
        List<Cheese> cheeses = List.of(cheese);
        when(cheeseRepository.findCheesesByCriteria(anyString(), anyString(), anyDouble()))
                .thenReturn(cheeses);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.searchCheesesJpql("Италия", "Твердые", 35.0);

        assertThat(result).hasSize(1);
        verify(cheeseRepository).findCheesesByCriteria("Италия", "Твердые", 35.0);
    }

    @Test
    void searchCheesesJpql_ShouldReturnEmptyList_WhenNoCheesesFound() {
        when(cheeseRepository.findCheesesByCriteria(anyString(), anyString(), anyDouble()))
                .thenReturn(List.of());

        List<CheeseResponseDto> result = cheeseService.searchCheesesJpql("Франция", "Мягкие", 30.0);

        assertThat(result).isEmpty();
        verify(cheeseRepository).findCheesesByCriteria("Франция", "Мягкие", 30.0);
    }

    @Test
    void searchCheesesNative_ShouldReturnListOfCheeses() {
        List<Cheese> cheeses = List.of(cheese);
        when(cheeseRepository.findCheesesByCriteriaNative(anyString(), anyString(), anyDouble()))
                .thenReturn(cheeses);
        when(cheeseMapper.toResponseDto(cheese)).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.searchCheesesNative("Италия", "Твердые", 35.0);

        assertThat(result).hasSize(1);
        verify(cheeseRepository).findCheesesByCriteriaNative("Италия", "Твердые", 35.0);
    }

    @Test
    void searchCheesesNative_ShouldReturnEmptyList_WhenNoCheesesFound() {
        when(cheeseRepository.findCheesesByCriteriaNative(anyString(), anyString(), anyDouble()))
                .thenReturn(List.of());

        List<CheeseResponseDto> result = cheeseService.searchCheesesNative("Франция", "Мягкие", 30.0);

        assertThat(result).isEmpty();
        verify(cheeseRepository).findCheesesByCriteriaNative("Франция", "Мягкие", 30.0);
    }

    @Test
    void bulkCreateCheesesWithTx_ShouldCreateAllCheeses_WhenNoProblem() {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(cheeseMapper.toEntity(any(CheeseRequestDto.class))).thenReturn(cheese);
        when(cheeseRepository.save(any(Cheese.class))).thenReturn(cheese);
        when(cheeseMapper.toResponseDto(any(Cheese.class))).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.bulkCreateCheesesWithTx(1L, 1L, cheeseBulkRequestDto);

        assertThat(result).hasSize(2);
        verify(cheeseRepository, times(2)).save(any(Cheese.class));
    }

    @Test
    void bulkCreateCheesesWoTx_ShouldCreateAllCheeses_WhenNoProblem() {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(cheeseMapper.toEntity(any(CheeseRequestDto.class))).thenReturn(cheese);
        when(cheeseRepository.save(any(Cheese.class))).thenReturn(cheese);
        when(cheeseMapper.toResponseDto(any(Cheese.class))).thenReturn(cheeseResponseDto);

        List<CheeseResponseDto> result = cheeseService.bulkCreateCheesesWoTx(1L, 1L, cheeseBulkRequestDto);

        assertThat(result).hasSize(2);
        verify(cheeseRepository, times(2)).save(any(Cheese.class));
    }

    @Test
    void bulkCreateCheesesWithTx_ShouldThrowException_WhenShopNotFound() {
        when(shopRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.bulkCreateCheesesWithTx(99L, 1L, cheeseBulkRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Магазин не найден с id: 99");

        verify(cheeseRepository, never()).save(any(Cheese.class));
    }

    @Test
    void bulkCreateCheesesWoTx_ShouldThrowException_WhenProducerNotFound() {
        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(producerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cheeseService.bulkCreateCheesesWoTx(1L, 99L, cheeseBulkRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Производитель не найден с id: 99");

        verify(cheeseRepository, never()).save(any(Cheese.class));
    }

    @Test
    void createCheesesAsync_ShouldCreatePendingTaskAndStartExecutor() {
        String taskId = cheeseService.createCheesesAsync(1L, 1L, cheeseBulkRequestDto);

        assertThat(taskId).isNotBlank();
        verify(asyncTaskStorage).saveTask(any(AsyncTask.class));
        verify(asyncCheeseExecutorService).executeCheesesCreation(
                anyString(),
                eq(1L),
                eq(1L),
                eq(cheeseBulkRequestDto.getCheeses())
        );
    }

    @Test
    void getCheeseTaskStatus_ShouldReturnTaskFromStorage() {
        AsyncTask task = AsyncTask.builder()
                .taskId("task-1")
                .status(AsyncTaskStatus.IN_PROGRESS)
                .progress(50)
                .build();
        when(asyncTaskStorage.getTask("task-1")).thenReturn(task);

        AsyncTask result = cheeseService.getCheeseTaskStatus("task-1");

        assertThat(result).isEqualTo(task);
        verify(asyncTaskStorage).getTask("task-1");
    }

    @Test
    void getAllAsyncTasks_ShouldReturnMapFromStorage() {
        Map<String, AsyncTask> tasks = Map.of(
                "task-1",
                AsyncTask.builder().taskId("task-1").status(AsyncTaskStatus.PENDING).progress(0).build()
        );
        when(asyncTaskStorage.getAllTasks()).thenReturn(tasks);

        Map<String, AsyncTask> result = cheeseService.getAllAsyncTasks();

        assertThat(result).isEqualTo(tasks);
        verify(asyncTaskStorage).getAllTasks();
    }
}
