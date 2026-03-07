package com.ryabaya.cheese.cache;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.function.Supplier;

@Component
public class IndexManager {

    private final Map<IndexKey, Object> storage = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(IndexKey key, Supplier<T> supplier) {
        if (storage.containsKey(key)) {
            return (T) storage.get(key);
        }
        T result = supplier.get();
        storage.put(key, result);
        return result;
    }

    public void invalidate(Class<?>... entityClasses) {
        var classesList = Arrays.asList(entityClasses);
        storage.keySet().removeIf(key -> classesList.contains(key.getEntityClass()));
    }
}