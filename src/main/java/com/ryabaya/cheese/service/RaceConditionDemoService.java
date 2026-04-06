package com.ryabaya.cheese.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaceConditionDemoService {

    private static final int THREAD_COUNT = 50;
    private static final int INCREMENTS_PER_THREAD = 1000;
    private static final int EXPECTED_VALUE = THREAD_COUNT * INCREMENTS_PER_THREAD;

    private final CounterService counterService;

    public void demonstrateRaceCondition() throws InterruptedException {
        log.info("RACE CONDITION:");
        counterService.reset();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counterService.incrementUnsafe();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int actualValue = counterService.getUnsafeValue();
        logResults("Небезопасный счётчик", actualValue);
    }

    public void demonstrateSynchronizedSolution() throws InterruptedException {
        log.info("\nSYNCHRONIZED:");
        counterService.reset();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counterService.incrementSynchronized();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int actualValue = counterService.getSynchronizedValue();
        logResults("Синхронизированный счётчик", actualValue);
    }

    public void demonstrateAtomicSolution() throws InterruptedException {
        log.info("\nATOMICINTEGER:");
        counterService.reset();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counterService.increment();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int actualValue = counterService.getValue();
        logResults("Atomic счётчик", actualValue);
    }

    public void runAllDemos() throws InterruptedException {
        log.info("Запуск демонстрации race condition с {} потоками", THREAD_COUNT);
        log.info("Каждый поток выполняет {} инкрементов", INCREMENTS_PER_THREAD);
        log.info("Ожидаемое значение: {}\n", EXPECTED_VALUE);

        demonstrateRaceCondition();
        demonstrateSynchronizedSolution();
        demonstrateAtomicSolution();
    }

    private void logResults(String counterName, int actualValue) {
        log.info("{}:", counterName);
        log.info("  Ожидаемое значение: {}", EXPECTED_VALUE);
        log.info("  Фактическое значение: {}", actualValue);

        if (counterName.equals("Небезопасный счётчик")) {
            log.info("  Потеряно обновлений: {}", EXPECTED_VALUE - actualValue);
            log.info("  Race condition: {}", actualValue != EXPECTED_VALUE ? "ПРИСУТСТВУЕТ" : "ОТСУТСТВУЕТ");
        } else {
            log.info("  Результат: {}", actualValue == EXPECTED_VALUE ? "УСПЕХ" : "НЕУДАЧА");
        }
    }
}
