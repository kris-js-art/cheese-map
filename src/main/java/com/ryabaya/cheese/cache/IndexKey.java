package com.ryabaya.cheese.cache;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class IndexKey {
    private final Class<?> entityClass;
    private final String methodName;
    private final List<Object> args;

    public IndexKey(Class<?> entityClass, String methodName, Object... args) {
        this.entityClass = entityClass;
        this.methodName = methodName;
        this.args = List.of(args);
    }
}