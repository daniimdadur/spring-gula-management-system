package com.guvaren.gms.notification.dto.request;

import com.guvaren.gms.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestNotificationRequest {

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Recipient is required")
    private String recipient;
}
