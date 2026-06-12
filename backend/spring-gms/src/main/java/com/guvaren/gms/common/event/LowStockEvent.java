package com.guvaren.gms.common.event;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    @Builder.Default
    private Instant eventTimestamp = Instant.now();
    private UUID inventoryId;
    private UUID productId;
    private String productName;
    private Integer currentStock;
    private Integer minimumStock;
}
