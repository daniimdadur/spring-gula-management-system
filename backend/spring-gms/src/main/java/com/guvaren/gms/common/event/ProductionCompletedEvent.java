package com.guvaren.gms.common.event;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionCompletedEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();
    @Builder.Default
    private Instant eventTimestamp = Instant.now();
    private UUID productionId;
    private UUID productId;
    private Integer quantity;
    private LocalDate productionDate;
    private String notes;
}
