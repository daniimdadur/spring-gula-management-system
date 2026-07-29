package com.guvaren.gms.master.dashboard.event;

import com.guvaren.gms.common.event.InventoryUpdatedEvent;
import com.guvaren.gms.master.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardEventListener {

    private final DashboardService dashboardService;

    @Async
    @EventListener
    public void handleInventoryUpdated(InventoryUpdatedEvent event) {
        log.info("Evicting dashboard cache due to InventoryUpdatedEvent: inventoryId={}", event.getInventoryId());
        try {
            dashboardService.evictDashboardCache();
        } catch (Exception e) {
            log.error("Failed to evict dashboard cache: inventoryId={}", event.getInventoryId(), e);
        }
    }
}
