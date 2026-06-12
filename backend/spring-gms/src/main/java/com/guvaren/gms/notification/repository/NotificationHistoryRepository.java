package com.guvaren.gms.notification.repository;

import com.guvaren.gms.notification.entity.NotificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {
    Page<NotificationHistory> findAllByOrderBySentAtDesc(Pageable pageable);
}
