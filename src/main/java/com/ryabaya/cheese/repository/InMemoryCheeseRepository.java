package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Cheese;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryCheeseRepository {

    private final List<Cheese> storage = new ArrayList<>();

    public InMemoryCheeseRepository() {
        storage.add(new Cheese(1L, "Пармезан", 32.0));
        storage.add(new Cheese(2L, "Моцарелла", 22.0));
        storage.add(new Cheese(3L, "Чеддер", 33.0));
        storage.add(new Cheese(4L, "Гауда", 28.0));
        storage.add(new Cheese(5L, "Камамбер", 25.0));
        storage.add(new Cheese(6L, "Бри", 27.0));
        storage.add(new Cheese(7L, "Рокфор", 30.0));
        storage.add(new Cheese(8L, "Фета", 21.0));
        storage.add(new Cheese(9L, "Маасдам", 27.0));
        storage.add(new Cheese(10L, "Козий сыр", 24.0));
    }

    public Cheese findById(Long id) {
        for (Cheese cheese : storage) {
            if (cheese.getId().equals(id)) {
                return cheese;
            }
        }
        return null;
    }

    public Cheese findByName(String name) {
        for (Cheese cheese : storage) {
            if (cheese.getName().equals(name)) {
                return cheese;
            }
        }
        return null;
    }
}