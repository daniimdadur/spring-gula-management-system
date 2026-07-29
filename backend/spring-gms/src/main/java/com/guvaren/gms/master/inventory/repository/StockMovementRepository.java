package com.guvaren.gms.master.inventory.repository;

import com.guvaren.gms.master.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    Page<StockMovement> findByInventoryIdOrderByCreatedAtDesc(UUID inventoryId, Pageable pageable);
}
