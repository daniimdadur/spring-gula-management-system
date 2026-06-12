package com.guvaren.gms.common.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAllocationFailedEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    @Builder.Default
    private Instant eventTimestamp = Instant.now();
    private UUID paymentId;
    private UUID orderId;
    private UUID productId;
    private Integer requestedQuantity;
    private Integer availableStock;
    private BigDecimal orderAmount;
    private String reason;
}
