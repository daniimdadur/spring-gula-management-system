package com.guvaren.gms.master.production.dto.response;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private LocalDate productionDate;
    private String notes;
    private Instant createdAt;
}
