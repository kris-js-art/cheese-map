package com.ryabaya.cheese.service;

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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProducerService {

    private final ProducerRepository producerRepository;
    private final ProducerMapper producerMapper;

    public ProducerResponseDto createProducer(ProducerRequestDto producerDto) {
        Producer producer = producerMapper.toEntity(producerDto);
        Producer savedProducer = producerRepository.save(producer);
        return producerMapper.toResponseDto(savedProducer);
    }

    @Transactional(readOnly = true)
    public ProducerResponseDto getProducerById(Long id) {
        Producer producer = producerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + id));
        return producerMapper.toResponseDto(producer);
    }

    @Transactional(readOnly = true)
    public List<ProducerResponseDto> getAllProducers() {
        return producerRepository.findAll().stream()
                .map(producerMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public ProducerResponseDto updateProducer(Long id, ProducerRequestDto producerDto) {
        Producer producer = producerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with id: " + id));

        producerMapper.updateEntityFromDto(producer, producerDto);
        Producer updatedProducer = producerRepository.save(producer);
        return producerMapper.toResponseDto(updatedProducer);
    }

    public void deleteProducer(Long id) {
        if (!producerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producer not found with id: " + id);
        }
        producerRepository.deleteById(id);
    }
}
