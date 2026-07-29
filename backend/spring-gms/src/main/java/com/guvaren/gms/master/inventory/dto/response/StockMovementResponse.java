package com.guvaren.gms.master.inventory.dto.response;

import com.guvaren.gms.master.inventory.entity.MovementType;
import com.guvaren.gms.master.inventory.entity.ReferenceType;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private UUID id;
    private MovementType movementType;
    private Integer quantity;
    private ReferenceType referenceType;
    private UUID referenceId;
    private String notes;
    private Instant createdAt;
}
