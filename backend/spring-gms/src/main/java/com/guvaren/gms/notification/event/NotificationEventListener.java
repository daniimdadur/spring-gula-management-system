package com.guvaren.gms.notification.event;

import com.guvaren.gms.common.event.LowStockEvent;
import com.guvaren.gms.common.event.OrderCreatedEvent;
import com.guvaren.gms.common.event.PaymentSuccessEvent;
import com.guvaren.gms.notification.entity.NotificationStatus;
import com.guvaren.gms.notification.entity.NotificationType;
import com.guvaren.gms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Logging notification for OrderCreatedEvent: orderId={}", event.getOrderId());
        try {
            notificationService.logNotification(
                    "admin-penjualan@gulahub.com",
                    NotificationType.ORDER_CREATED,
                    "Pesanan baru dibuat dengan ID: " + event.getOrderId() + ", total: Rp " + event.getTotalAmount(),
                    NotificationStatus.SENT
            );
        } catch (Exception e) {
            log.error("Failed to log order created notification: orderId={}", event.getOrderId(), e);
        }
    }

    @Async
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Logging notification for PaymentSuccessEvent: paymentId={}", event.getPaymentId());
        try {
            notificationService.logNotification(
                    "admin-penjualan@gulahub.com",
                    NotificationType.PAYMENT_SUCCESS,
                    "Pembayaran sukses untuk order ID: " + event.getOrderId() + ", jumlah: Rp " + event.getAmount(),
                    NotificationStatus.SENT
            );
        } catch (Exception e) {
            log.error("Failed to log payment success notification: paymentId={}", event.getPaymentId(), e);
        }
    }

    @Async
    @EventListener
    public void handleLowStock(LowStockEvent event) {
        log.info("Logging notification for LowStockEvent: productId={}, stock={}",
                event.getProductId(), event.getCurrentStock());
        try {
            notificationService.logNotification(
                    "owner@gulahub.com",
                    NotificationType.LOW_STOCK,
                    "Stok menipis: " + event.getProductName() + ", sisa stok: " + event.getCurrentStock() +
                            " (minimal: " + event.getMinimumStock() + ")",
                    NotificationStatus.SENT
            );
        } catch (Exception e) {
            log.error("Failed to log low stock notification: productId={}", event.getProductId(), e);
        }
    }
}
