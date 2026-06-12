package com.guvaren.gms.product.repository;

import com.guvaren.gms.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT p FROM Product p WHERE " +
           "(:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:category IS NULL OR p.category = :category)")
    Page<Product> findBySearchAndCategory(@Param("q") String q, @Param("category") String category, Pageable pageable);
}
