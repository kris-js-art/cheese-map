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

    private static final int THREAD_COUNT = 50; // количество потоков
    private static final int INCREMENTS_PER_THREAD = 1000; // число до которого каждый поток будет увеличивать счетчик
    private static final int EXPECTED_VALUE = THREAD_COUNT * INCREMENTS_PER_THREAD; // ожидаемое значение(каждый поток досчитает до 1000 а у нас всего 50 потоков, значит ожидаемое значение будет 50000)

    private final CounterService counterService;

    public void demonstrateRaceCondition() throws InterruptedException { // функция для работы с небезопасным счетчиком
        log.info("RACE CONDITION:");
        counterService.reset();// сбрасываем все счетчикик

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT); // создаем 50 потоков, каждый их которых будет увеличивать значение счетчика на 1

        // цикл в котором будем небезопасный счетчик
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counterService.incrementUnsafe();// вызываем функцию для увеличения на 1 небезопасного счетчика
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int actualValue = counterService.getUnsafeValue();
        logResults("Небезопасный счётчик", actualValue);// выводим результат
    }

    public void demonstrateSynchronizedSolution() throws InterruptedException { //функция для работы со счетчиком с synchronized
        log.info("\nSYNCHRONIZED:");
        counterService.reset();// сбрасываем все счетчикик

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT); // также создаем 50 потоков как и в методе выше

        // цикл в котором будем увеличивать счетчик с synchronized
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
        logResults("Синхронизированный счётчик", actualValue);// выводим результат
    }

    public void demonstrateAtomicSolution() throws InterruptedException {// функция для работы с атомик счетчиком
        log.info("\nATOMICINTEGER:");
        counterService.reset();// сбрасываем все счетчикик

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);// также создаем 50 потоков как и в методе выше

        // цикл в котором будем увеличивать атомик счетчик
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
        logResults("Atomic счётчик", actualValue);// выводим результат
    }

    public void runAllDemos() throws InterruptedException {// функция которая запускает демонстрацию работы всех счетчиков

        // вывод в консоль о том что запускаем демонстрацию
        log.info("Запуск демонстрации race condition с {} потоками", THREAD_COUNT);
        log.info("Каждый поток выполняет {} инкрементов", INCREMENTS_PER_THREAD);
        log.info("Ожидаемое значение: {}\n", EXPECTED_VALUE);

        demonstrateRaceCondition();// запускаем небезопасный счетчик
        demonstrateSynchronizedSolution();// запускаем счетчик с ynchronized
        demonstrateAtomicSolution();// запускаем атомик счетчик
    }

    private void logResults(String counterName, int actualValue) {// функция для вывода информации о счетчике
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
