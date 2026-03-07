package com.ryabaya.cheese.repository;

import com.ryabaya.cheese.entity.Cheese;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CheeseRepository extends JpaRepository<Cheese, Long> {
    Optional<Cheese> findByName(String name);

    List<Cheese> findByProducerId(Long producerId);

    @EntityGraph(attributePaths = {"producer", "shop", "categories", "reviews"})
    @Query("select c from Cheese c")
    List<Cheese> findAllWithGraph();

    @Query("SELECT DISTINCT c FROM Cheese c "
            + "LEFT JOIN c.producer p "
            + "LEFT JOIN c.categories cat "
            + "WHERE (:producerCountry IS NULL OR p.country = :producerCountry) "
            + "AND (:categoryName IS NULL OR cat.name = :categoryName) "
            + "AND (:maxFats IS NULL OR c.fats <= :maxFats) "
            + "ORDER BY c.name")
    List<Cheese> findCheesesByCriteria(
            @Param("producerCountry") String producerCountry,
            @Param("categoryName") String categoryName,
            @Param("maxFats") Double maxFats
    );

    @Query(value = "SELECT DISTINCT c.* FROM cheeses c "
            + "LEFT JOIN producers p ON c.producer_id = p.id "
            + "LEFT JOIN cheese_categories cc ON c.id = cc.cheese_id "
            + "LEFT JOIN categories cat ON cc.category_id = cat.id "
            + "WHERE (:producerCountry IS NULL OR p.country = :producerCountry) "
            + "AND (:categoryName IS NULL OR cat.name = :categoryName) "
            + "AND (:maxFats IS NULL OR c.fats <= CAST(:maxFats AS DECIMAL)) "
            + "ORDER BY c.name",
            nativeQuery = true)
    List<Cheese> findCheesesByCriteriaNative(
            @Param("producerCountry") String producerCountry,
            @Param("categoryName") String categoryName,
            @Param("maxFats") Double maxFats
    );
}