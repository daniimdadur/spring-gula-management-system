package com.guvaren.gms.master.payment.event;

import com.guvaren.gms.common.event.StockAllocationFailedEvent;
import com.guvaren.gms.master.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @Async
    @EventListener
    @Transactional
    public void handleStockAllocationFailed(StockAllocationFailedEvent event) {
        log.info("Handling StockAllocationFailedEvent for payment: paymentId={}, orderId={}",
                event.getPaymentId(), event.getOrderId());
        try {
            paymentService.markPaymentAsFailed(event.getPaymentId());
            log.info("Payment marked as FAILED: paymentId={}", event.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to mark payment as FAILED: paymentId={}", event.getPaymentId(), e);
        }
    }
}
