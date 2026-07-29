package com.guvaren.gms.master.order.event;

import com.guvaren.gms.common.event.InventoryUpdatedEvent;
import com.guvaren.gms.common.event.StockAllocationFailedEvent;
import com.guvaren.gms.master.inventory.entity.ReferenceType;
import com.guvaren.gms.master.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;

    @Async
    @EventListener
    @Transactional
    public void handleInventoryUpdated(InventoryUpdatedEvent event) {
        log.info("Handling InventoryUpdatedEvent: inventoryId={}, currentStock={}",
                event.getInventoryId(), event.getCurrentStock());

        if (event.getReferenceType() == ReferenceType.ORDER
                && event.getReferenceId() != null) {
            try {
                orderService.markOrderAsPaid(event.getReferenceId());
                log.info("Order marked as PAID: orderId={}", event.getReferenceId());
            } catch (Exception e) {
                log.error("Failed to mark order as PAID: orderId={}", event.getReferenceId(), e);
            }
        }
    }

    @Async
    @EventListener
    @Transactional
    public void handleStockAllocationFailed(StockAllocationFailedEvent event) {
        log.info("Handling StockAllocationFailedEvent: orderId={}, reason={}",
                event.getOrderId(), event.getReason());
        try {
            orderService.cancelOrder(event.getOrderId(), "Pembatalan otomatis akibat kegagalan alokasi stok: " + event.getReason());
            log.info("Order cancelled due to stock allocation failure: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to cancel order: orderId={}", event.getOrderId(), e);
        }
    }
}
