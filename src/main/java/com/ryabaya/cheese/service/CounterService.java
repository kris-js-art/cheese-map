package com.ryabaya.cheese.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CounterService { // класс для реализации всех видов счетчиков

    private final AtomicInteger atomicCounter = new AtomicInteger(0); // атомик счетчик
    private int unsafeCounter = 0; // небезопасный счетчик
    private int synchronizedCounter = 0; // счетчик с synchronized

    public void increment() {
        atomicCounter.incrementAndGet();
    } // метод для увеличения атомик счетчика на 1

    public int getValue() {
        return atomicCounter.get();
    } // метод для получения значния атомик счетчика

    public void reset() { // метод для сброса значения всех счетчиков
        atomicCounter.set(0);
        unsafeCounter = 0;
        synchronizedCounter = 0;
    }

    public void incrementUnsafe() {
        unsafeCounter++;
    } // метод для увеличения небезопасного счетчика на 1

    public int getUnsafeValue() {
        return unsafeCounter;
    } // метод для получения значения небезопасного счетчика

    public synchronized void incrementSynchronized() {
        synchronizedCounter++;
    } // метод для увеличения synchronized счетчика на 1(в этом методе используется ключевое слово synchronized которое делает увеличение на 1 безопасным )

    public int getSynchronizedValue() {
        return synchronizedCounter;
    }// метод для получения значения synchronized счетчика
}
