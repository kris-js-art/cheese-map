package com.ryabaya.cheese.service;

import com.ryabaya.cheese.cache.IndexKey;
import com.ryabaya.cheese.cache.IndexManager;
import com.ryabaya.cheese.dto.request.ProducerRequestDto;
import com.ryabaya.cheese.dto.response.ProducerResponseDto;
import com.ryabaya.cheese.entity.Producer;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.ProducerMapper;
import com.ryabaya.cheese.repository.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProducerService {

    private final IndexManager indexManager;
    private final ProducerRepository producerRepository;
    private final ProducerMapper producerMapper;

    private static final String GET_ALL = "getAllProducers";
    private static final String GET_BY_ID = "getById";

    public ProducerResponseDto createProducer(ProducerRequestDto producerDto) {
        indexManager.invalidate(Producer.class);

        Producer producer = producerMapper.toEntity(producerDto);
        Producer savedProducer = producerRepository.save(producer);
        return producerMapper.toResponseDto(savedProducer);
    }

    @Transactional(readOnly = true)
    public ProducerResponseDto getProducerById(Long id) {
        IndexKey key = new IndexKey(Producer.class, GET_BY_ID, id);

        Producer producer = indexManager.computeIfAbsent(key, () ->
                producerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + id))
        );

        return producerMapper.toResponseDto(producer);
    }

    @Transactional(readOnly = true)
    public List<ProducerResponseDto> getAllProducers() {
        IndexKey key = new IndexKey(Producer.class, GET_ALL);

        return indexManager.computeIfAbsent(key, () ->
                producerRepository.findAll().stream()
                        .map(producerMapper::toResponseDto)
                        .toList()
        );
    }

    public ProducerResponseDto updateProducer(Long id, ProducerRequestDto producerDto) {
        indexManager.invalidate(Producer.class);

        Producer producer = producerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + id));

        producerMapper.updateEntityFromDto(producer, producerDto);
        Producer updatedProducer = producerRepository.save(producer);
        return producerMapper.toResponseDto(updatedProducer);
    }

    public void deleteProducer(Long id) {
        indexManager.invalidate(Producer.class);

        if (!producerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producer not found with id: " + id);
        }
        producerRepository.deleteById(id);
    }
}
