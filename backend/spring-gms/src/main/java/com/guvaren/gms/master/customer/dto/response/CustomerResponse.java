package com.guvaren.gms.master.customer.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private Instant createdAt;
}
