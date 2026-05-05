package com.ryabaya.cheese.service;

import com.ryabaya.cheese.dto.request.CheeseRequestDto;
import com.ryabaya.cheese.entity.Cheese;
import com.ryabaya.cheese.entity.Producer;
import com.ryabaya.cheese.entity.Shop;
import com.ryabaya.cheese.exception.InitiatedProblemException;
import com.ryabaya.cheese.exception.ResourceNotFoundException;
import com.ryabaya.cheese.mapper.CheeseMapper;
import com.ryabaya.cheese.entity.AsyncTask;
import com.ryabaya.cheese.entity.AsyncTaskStatus;
import com.ryabaya.cheese.repository.CheeseRepository;
import com.ryabaya.cheese.repository.ProducerRepository;
import com.ryabaya.cheese.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
// сервис для выполнения асинхронной задачи
public class AsyncCheeseExecutorService {

    private final AsyncTaskStorage asyncTaskStorage;
    private final CheeseRepository cheeseRepository;
    private final ShopRepository shopRepository;
    private final ProducerRepository producerRepository;
    private final CheeseMapper cheeseMapper;

    // метод для создания сыра
    @Async
    public void executeCheesesCreation(
            String taskId,
            Long shopId,
            Long producerId,
            List<CheeseRequestDto> cheeses) {
        AsyncTask task = asyncTaskStorage.getTask(taskId);
        task.setStatus(AsyncTaskStatus.IN_PROGRESS);

        try {
            Shop shop = shopRepository.findById(shopId).orElseThrow(
                    () -> new ResourceNotFoundException("Магазин не найден с id: " + shopId)
            );
            Producer producer = producerRepository.findById(producerId).orElseThrow(
                    () -> new ResourceNotFoundException(
                            "Производитель не найден с id: " + producerId
                    )
            );
            int total = cheeses.size();

            for (int i = 0; i < total; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    task.setStatus(AsyncTaskStatus.FAILED);
                    task.setEndTime(LocalDateTime.now());
                    task.setResult("Задача была прервана");
                    return;
                }

                CheeseRequestDto request = cheeses.get(i);
                Cheese cheese = cheeseMapper.toEntity(request);
                cheese.setShop(shop);
                cheese.setProducer(producer);
                cheeseRepository.save(cheese);

                int progress = (i + 1) * 100 / total;
                task.setProgress(progress);

                sleepWithInterruptionHandling(task);
            }

            task.setStatus(AsyncTaskStatus.COMPLETED);
            task.setEndTime(LocalDateTime.now());
            task.setProgress(100);
            task.setResult("Создано " + total + " сыров");
        } catch (Exception e) {
            task.setStatus(AsyncTaskStatus.FAILED);
            task.setEndTime(LocalDateTime.now());
            task.setResult("Ошибка: " + e.getMessage());
        }
    }

    private void sleepWithInterruptionHandling(AsyncTask task) {
        // метод для создания задержки после создания каждого сыра
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(AsyncTaskStatus.FAILED);
            task.setEndTime(LocalDateTime.now());
            task.setResult("Задача была прервана во время ожидания");
            throw new InitiatedProblemException("Task interrupted");
        }
    }
}
