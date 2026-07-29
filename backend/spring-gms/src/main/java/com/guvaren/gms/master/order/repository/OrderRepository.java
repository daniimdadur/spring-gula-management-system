package com.guvaren.gms.master.order.repository;

import com.guvaren.gms.master.order.entity.Order;
import com.guvaren.gms.master.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByStatusFilter(@Param("status") OrderStatus status, Pageable pageable);
}
