package com.guvaren.gms.order.service;

import com.guvaren.gms.common.event.InventoryUpdatedEvent;
import com.guvaren.gms.common.event.OrderCreatedEvent;
import com.guvaren.gms.common.event.StockAllocationFailedEvent;
import com.guvaren.gms.customer.entity.Customer;
import com.guvaren.gms.customer.repository.CustomerRepository;
import com.guvaren.gms.inventory.service.InventoryService;
import com.guvaren.gms.order.dto.request.OrderRequest;
import com.guvaren.gms.order.dto.request.UpdateOrderStatusRequest;
import com.guvaren.gms.order.dto.response.OrderResponse;
import com.guvaren.gms.order.entity.Order;
import com.guvaren.gms.order.entity.OrderItem;
import com.guvaren.gms.order.entity.OrderStatus;
import com.guvaren.gms.order.repository.OrderRepository;
import com.guvaren.gms.product.entity.Product;
import com.guvaren.gms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id: " + request.getCustomerId()));

        for (OrderRequest.OrderItemRequest item : request.getItems()) {
            if (!inventoryService.hasSufficientStock(item.getProductId(), item.getQuantity())) {
                Product product = productRepository.findById(item.getProductId())
                        .orElse(null);
                String productName = (product != null) ? product.getName() : item.getProductId().toString();
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stok tidak mencukupi untuk produk: " + productName);
            }
        }

        Order order = Order.builder()
                .customer(customer)
                .orderDate(java.time.Instant.now())
                .status(OrderStatus.CREATED)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            BigDecimal subtotal = itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(productRepository.getReferenceById(itemReq.getProductId()))
                    .productName(itemReq.getProductName())
                    .price(itemReq.getPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();
            order.getItems().add(orderItem);
        }
        order.setTotalAmount(totalAmount);

        order = orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(customer.getId())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItemPayload.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .orderDate(order.getOrderDate())
                .build();
        eventPublisher.publishEvent(event);

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + id));

        order.setStatus(request.getStatus());
        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusFilter(status, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void markOrderAsPaid(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .build();
    }
}
