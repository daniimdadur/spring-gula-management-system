package com.guvaren.gms.master.notification.service;

import com.guvaren.gms.master.notification.dto.request.TestNotificationRequest;
import com.guvaren.gms.master.notification.dto.response.NotificationHistoryResponse;
import com.guvaren.gms.master.notification.entity.NotificationHistory;
import com.guvaren.gms.master.notification.entity.NotificationStatus;
import com.guvaren.gms.master.notification.entity.NotificationType;
import com.guvaren.gms.master.notification.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    @Transactional(readOnly = true)
    public Page<NotificationHistoryResponse> getAllNotifications(Pageable pageable) {
        return notificationHistoryRepository.findAllByOrderBySentAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void sendTestNotification(TestNotificationRequest request) {
        NotificationHistory history = NotificationHistory.builder()
                .recipient(request.getRecipient())
                .type(request.getType())
                .message(request.getMessage())
                .status(NotificationStatus.SENT)
                .build();
        notificationHistoryRepository.save(history);
    }

    @Transactional
    public void logNotification(String recipient, NotificationType type, String message, NotificationStatus status) {
        NotificationHistory history = NotificationHistory.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .status(status)
                .build();
        notificationHistoryRepository.save(history);
    }

    private NotificationHistoryResponse mapToResponse(NotificationHistory history) {
        return NotificationHistoryResponse.builder()
                .id(history.getId())
                .recipient(history.getRecipient())
                .type(history.getType())
                .message(history.getMessage())
                .status(history.getStatus())
                .sentAt(history.getSentAt())
                .build();
    }
}
