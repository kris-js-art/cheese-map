package com.ryabaya.cheese.service;

import com.ryabaya.cheese.cache.IndexKey;
import com.ryabaya.cheese.cache.IndexManager;
import com.ryabaya.cheese.dto.request.ProducerRequestDto;
import com.ryabaya.cheese.dto.response.ProducerResponseDto;
import com.ryabaya.cheese.entity.Producer;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.ProducerMapper;
import com.ryabaya.cheese.repository.ProducerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerServiceTest {

    @Mock
    private IndexManager indexManager;

    @Mock
    private ProducerRepository producerRepository;

    @Mock
    private ProducerMapper producerMapper;

    @InjectMocks
    private ProducerService producerService;

    private Producer producer;
    private ProducerRequestDto producerRequestDto;
    private ProducerResponseDto producerResponseDto;

    @BeforeEach
    void setUp() {
        producer = new Producer();
        producer.setId(1L);
        producer.setName("Parmigiano Reggiano");
        producer.setCountry("Италия");
        producer.setDescription("Известный производитель");

        producerRequestDto = new ProducerRequestDto();
        producerRequestDto.setName("Parmigiano Reggiano");
        producerRequestDto.setCountry("Италия");
        producerRequestDto.setDescription("Известный производитель");

        producerResponseDto = new ProducerResponseDto();
        producerResponseDto.setId(1L);
        producerResponseDto.setName("Parmigiano Reggiano");
        producerResponseDto.setCountry("Италия");
        producerResponseDto.setDescription("Известный производитель");
    }

    @Test
    void createProducer_ShouldReturnProducerResponseDto() {
        when(producerMapper.toEntity(producerRequestDto)).thenReturn(producer);
        when(producerRepository.save(any(Producer.class))).thenReturn(producer);
        when(producerMapper.toResponseDto(producer)).thenReturn(producerResponseDto);
        doNothing().when(indexManager).invalidate(Producer.class);

        ProducerResponseDto result = producerService.createProducer(producerRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Parmigiano Reggiano");
        verify(indexManager).invalidate(Producer.class);
        verify(producerRepository).save(any(Producer.class));
    }

    @Test
    void getProducerById_ShouldReturnProducerResponseDto_WhenProducerExists() {
        IndexKey key = new IndexKey(Producer.class, "getById", 1L);

        when(indexManager.computeIfAbsent(eq(key), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<Producer> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(producerRepository.findById(1L)).thenReturn(Optional.of(producer));
        when(producerMapper.toResponseDto(producer)).thenReturn(producerResponseDto);

        ProducerResponseDto result = producerService.getProducerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(indexManager).computeIfAbsent(eq(key), any(Supplier.class));
        verify(producerRepository).findById(1L);
    }

    @Test
    void getProducerById_ShouldThrowException_WhenProducerNotFound() {
        IndexKey key = new IndexKey(Producer.class, "getById", 99L);

        when(indexManager.computeIfAbsent(eq(key), any(Supplier.class)))
                .thenThrow(new ResourceNotFoundException("Producer not found with id: 99"));

        assertThatThrownBy(() -> producerService.getProducerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producer not found with id: 99");

        verify(indexManager).computeIfAbsent(eq(key), any(Supplier.class));
        verify(producerRepository, never()).findById(99L);
    }

    @Test
    void getAllProducers_ShouldReturnListOfProducers() {
        IndexKey key = new IndexKey(Producer.class, "getAllProducers");
        List<Producer> producers = List.of(producer);

        when(indexManager.computeIfAbsent(eq(key), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<List<ProducerResponseDto>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        when(producerRepository.findAll()).thenReturn(producers);
        when(producerMapper.toResponseDto(producer)).thenReturn(producerResponseDto);

        List<ProducerResponseDto> result = producerService.getAllProducers();

        assertThat(result).hasSize(1);
        verify(indexManager).computeIfAbsent(eq(key), any(Supplier.class));
        verify(producerRepository).findAll();
    }

    @Test
    void updateProducer_ShouldReturnUpdatedProducerResponseDto_WhenProducerExists() {
        Producer existingProducer = new Producer();
        existingProducer.setId(1L);
        existingProducer.setName("Старый производитель");

        when(producerRepository.findById(1L)).thenReturn(Optional.of(existingProducer));
        doAnswer(invocation -> {
            existingProducer.setName(producerRequestDto.getName());
            existingProducer.setCountry(producerRequestDto.getCountry());
            existingProducer.setDescription(producerRequestDto.getDescription());
            return null;
        }).when(producerMapper).updateEntityFromDto(existingProducer, producerRequestDto);
        when(producerRepository.save(existingProducer)).thenReturn(producer);
        when(producerMapper.toResponseDto(producer)).thenReturn(producerResponseDto);
        doNothing().when(indexManager).invalidate(Producer.class);

        ProducerResponseDto result = producerService.updateProducer(1L, producerRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Parmigiano Reggiano");
        verify(indexManager).invalidate(Producer.class);
        verify(producerRepository).findById(1L);
        verify(producerMapper).updateEntityFromDto(existingProducer, producerRequestDto);
        verify(producerRepository).save(existingProducer);
    }

    @Test
    void updateProducer_ShouldThrowException_WhenProducerNotFound() {
        when(producerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> producerService.updateProducer(99L, producerRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producer not found with id: 99");

        verify(producerRepository).findById(99L);
        verify(producerMapper, never()).updateEntityFromDto(any(), any());
        verify(producerRepository, never()).save(any());
    }

    @Test
    void deleteProducer_ShouldCallDelete_WhenProducerExists() {
        when(producerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(producerRepository).deleteById(1L);
        doNothing().when(indexManager).invalidate(Producer.class);

        producerService.deleteProducer(1L);

        verify(indexManager).invalidate(Producer.class);
        verify(producerRepository).existsById(1L);
        verify(producerRepository).deleteById(1L);
    }

    @Test
    void deleteProducer_ShouldThrowException_WhenProducerNotFound() {
        when(producerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> producerService.deleteProducer(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producer not found with id: 99");

        verify(producerRepository).existsById(99L);
        verify(producerRepository, never()).deleteById(any());
    }
}