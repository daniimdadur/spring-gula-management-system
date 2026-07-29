package com.guvaren.gms.master.notification.dto.response;

import com.guvaren.gms.master.notification.entity.NotificationStatus;
import com.guvaren.gms.master.notification.entity.NotificationType;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryResponse {
    private UUID id;
    private String recipient;
    private NotificationType type;
    private String message;
    private NotificationStatus status;
    private Instant sentAt;
}
