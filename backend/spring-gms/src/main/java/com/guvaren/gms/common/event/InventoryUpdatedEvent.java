package com.guvaren.gms.common.event;

import com.guvaren.gms.inventory.entity.MovementType;
import com.guvaren.gms.inventory.entity.ReferenceType;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    @Builder.Default
    private Instant eventTimestamp = Instant.now();
    private UUID inventoryId;
    private UUID productId;
    private Integer previousStock;
    private Integer currentStock;
    private Integer minimumStock;
    private MovementType transactionType;
    private Integer changeQuantity;
    private ReferenceType referenceType;
    private UUID referenceId;
}
