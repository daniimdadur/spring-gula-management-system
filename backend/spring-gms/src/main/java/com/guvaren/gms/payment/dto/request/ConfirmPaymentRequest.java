package com.guvaren.gms.payment.dto.request;

import com.guvaren.gms.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequest {

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    private Instant paymentDate;
}
