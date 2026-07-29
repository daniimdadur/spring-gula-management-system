package com.guvaren.gms.master.payment.controller;

import com.guvaren.gms.master.payment.dto.request.ConfirmPaymentRequest;
import com.guvaren.gms.master.payment.dto.request.PaymentRequest;
import com.guvaren.gms.master.payment.dto.response.PaymentResponse;
import com.guvaren.gms.master.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PENJUALAN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("paymentId", response.getId(), "status", response.getStatus().name()));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN_PENJUALAN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> confirmPayment(@PathVariable UUID id,
                                                              @Valid @RequestBody ConfirmPaymentRequest request) {
        PaymentResponse response = paymentService.confirmPayment(id, request);
        return ResponseEntity.ok(Map.of(
                "paymentId", response.getId(),
                "status", response.getStatus().name(),
                "message", "Payment confirmed and event published."
        ));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_PENJUALAN')")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
