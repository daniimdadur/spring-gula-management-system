package com.guvaren.gms.master.notification.controller;

import com.guvaren.gms.master.notification.dto.request.TestNotificationRequest;
import com.guvaren.gms.master.notification.dto.response.NotificationHistoryResponse;
import com.guvaren.gms.master.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<NotificationHistoryResponse>> getAllNotifications(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Map<String, String>> sendTestNotification(@Valid @RequestBody TestNotificationRequest request) {
        notificationService.sendTestNotification(request);
        return ResponseEntity.ok(Map.of("message", "Test notification sent successfully"));
    }
}
