package com.guvaren.gms.inventory.event;

import com.guvaren.gms.common.event.PaymentSuccessEvent;
import com.guvaren.gms.common.event.ProductionCompletedEvent;
import com.guvaren.gms.common.event.StockAllocationFailedEvent;
import com.guvaren.gms.inventory.service.InventoryService;
import com.guvaren.gms.order.entity.Order;
import com.guvaren.gms.order.entity.OrderItem;
import com.guvaren.gms.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    @Transactional
    public void handleProductionCompleted(ProductionCompletedEvent event) {
        log.info("Handling ProductionCompletedEvent: productionId={}, productId={}, quantity={}",
                event.getProductionId(), event.getProductId(), event.getQuantity());
        try {
            inventoryService.addStockForProduction(event.getProductId(), event.getQuantity(), event.getProductionId());
            log.info("Stock added for production: productionId={}", event.getProductionId());
        } catch (Exception e) {
            log.error("Failed to add stock for production: productionId={}", event.getProductionId(), e);
        }
    }

    @Async
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Handling PaymentSuccessEvent: paymentId={}, orderId={}", event.getPaymentId(), event.getOrderId());
        try {
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            for (OrderItem item : order.getItems()) {
                inventoryService.deductStockForOrder(item.getProduct().getId(), item.getQuantity(), event.getOrderId());
            }
            log.info("Stock deducted for order: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to deduct stock for order: orderId={}, publishing StockAllocationFailedEvent", event.getOrderId(), e);
            eventPublisher.publishEvent(StockAllocationFailedEvent.builder()
                    .paymentId(event.getPaymentId())
                    .orderId(event.getOrderId())
                    .orderAmount(event.getAmount())
                    .reason(e.getMessage())
                    .build());
        }
    }
}
