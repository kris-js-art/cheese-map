package com.ryabaya.cheese.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cheese {

    private Long id;
    private String name;
    private Double fats;
}
