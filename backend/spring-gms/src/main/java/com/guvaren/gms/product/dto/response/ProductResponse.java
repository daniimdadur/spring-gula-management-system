package com.guvaren.gms.product.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String code;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private BigDecimal weight;
    private String imageUrl;
    private Boolean status;
}
