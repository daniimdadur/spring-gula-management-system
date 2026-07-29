package com.guvaren.gms.master.payment.service;

import com.guvaren.gms.common.event.PaymentSuccessEvent;
import com.guvaren.gms.master.order.entity.Order;
import com.guvaren.gms.master.order.repository.OrderRepository;
import com.guvaren.gms.master.payment.dto.request.ConfirmPaymentRequest;
import com.guvaren.gms.master.payment.dto.request.PaymentRequest;
import com.guvaren.gms.master.payment.dto.response.PaymentResponse;
import com.guvaren.gms.master.payment.entity.Payment;
import com.guvaren.gms.master.payment.entity.PaymentStatus;
import com.guvaren.gms.master.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with id: " + request.getOrderId()));

        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment already exists for this order");
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse confirmPayment(UUID id, ConfirmPaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found with id: " + id));

        payment.setStatus(request.getStatus());
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate());
        } else {
            payment.setPaymentDate(java.time.Instant.now());
        }

        payment = paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrder().getId())
                    .customerId(payment.getOrder().getCustomer().getId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentDate(payment.getPaymentDate())
                    .build();
            eventPublisher.publishEvent(event);
        }

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for order id: " + orderId));
        return mapToResponse(payment);
    }

    @Transactional
    public void markPaymentAsFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found with id: " + paymentId));
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
