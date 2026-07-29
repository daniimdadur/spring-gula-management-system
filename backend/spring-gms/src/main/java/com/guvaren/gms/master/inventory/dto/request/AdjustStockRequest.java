package com.guvaren.gms.master.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustStockRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String notes;
}
