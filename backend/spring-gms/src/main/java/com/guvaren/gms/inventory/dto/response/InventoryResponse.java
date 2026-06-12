package com.guvaren.gms.inventory.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private Integer currentStock;
    private Integer minimumStock;
    private Instant updatedAt;
}
