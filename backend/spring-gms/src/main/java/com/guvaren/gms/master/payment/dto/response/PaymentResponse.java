package com.guvaren.gms.master.payment.dto.response;

import com.guvaren.gms.master.payment.entity.PaymentMethod;
import com.guvaren.gms.master.payment.entity.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Instant paymentDate;
    private Instant createdAt;
}
