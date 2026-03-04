package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Cheese;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CheeseRepository extends JpaRepository<Cheese, Long> {
    Optional<Cheese> findByName(String name);

    List<Cheese> findByProducerId(Long producerId);

    @EntityGraph(attributePaths = {"producer", "shop", "categories", "reviews"})
    @Query("select c from Cheese c")
    List<Cheese> findAllWithGraph();

}