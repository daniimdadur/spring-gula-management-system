package com.guvaren.gms.master.order.controller;

import com.guvaren.gms.master.order.dto.request.OrderRequest;
import com.guvaren.gms.master.order.dto.request.UpdateOrderStatusRequest;
import com.guvaren.gms.master.order.dto.response.OrderResponse;
import com.guvaren.gms.master.order.entity.OrderStatus;
import com.guvaren.gms.master.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_PENJUALAN')")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "orderId", response.getId(),
                        "status", response.getStatus().name(),
                        "totalAmount", response.getTotalAmount(),
                        "message", "Order created successfully"
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_PENJUALAN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN_PENJUALAN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable UUID id,
                                                                  @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(Map.of(
                "orderId", response.getId(),
                "status", response.getStatus().name(),
                "message", "Order status updated successfully"
        ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_PENJUALAN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(@RequestParam(required = false) OrderStatus status,
                                                            Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }
}
