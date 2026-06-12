package com.guvaren.gms.production.repository;

import com.guvaren.gms.production.entity.Production;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ProductionRepository extends JpaRepository<Production, UUID> {

    @Query("SELECT p FROM Production p WHERE " +
           "(:productId IS NULL OR p.product.id = :productId) AND " +
           "(:startDate IS NULL OR p.productionDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.productionDate <= :endDate) " +
           "ORDER BY p.createdAt DESC")
    Page<Production> findByFilters(@Param("productId") UUID productId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   Pageable pageable);
}
