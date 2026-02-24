package com.ryabaya.cheese.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter//аннотация для создания get методов со всеми полями
@Setter//аннотация для создания set методов со всеми полями
@AllArgsConstructor// аннотация для создания конструктора со всеми параметрами
@NoArgsConstructor// аннотация для создания пустого конструктра
public class Cheese {

    private Long id;
    private String name;
    private Double fats;
}
