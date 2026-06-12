package com.guvaren.gms.inventory.controller;

import com.guvaren.gms.inventory.dto.request.AdjustStockRequest;
import com.guvaren.gms.inventory.dto.request.StockInRequest;
import com.guvaren.gms.inventory.dto.request.StockOutRequest;
import com.guvaren.gms.inventory.dto.response.InventoryResponse;
import com.guvaren.gms.inventory.dto.response.StockMovementResponse;
import com.guvaren.gms.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_GUDANG')")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PostMapping("/{inventoryId}/in")
    @PreAuthorize("hasRole('ADMIN_GUDANG')")
    public ResponseEntity<Map<String, Object>> stockIn(@PathVariable UUID inventoryId,
                                                       @Valid @RequestBody StockInRequest request) {
        InventoryResponse response = inventoryService.stockIn(inventoryId, request);
        return ResponseEntity.ok(Map.of(
                "inventoryId", response.getId(),
                "previousStock", response.getCurrentStock() - request.getQuantity(),
                "newStock", response.getCurrentStock(),
                "message", "Stock added successfully"
        ));
    }

    @PostMapping("/{inventoryId}/out")
    @PreAuthorize("hasRole('ADMIN_GUDANG')")
    public ResponseEntity<Map<String, Object>> stockOut(@PathVariable UUID inventoryId,
                                                        @Valid @RequestBody StockOutRequest request) {
        InventoryResponse response = inventoryService.stockOut(inventoryId, request);
        return ResponseEntity.ok(Map.of(
                "inventoryId", response.getId(),
                "previousStock", response.getCurrentStock() + request.getQuantity(),
                "newStock", response.getCurrentStock(),
                "message", "Stock reduced successfully"
        ));
    }

    @PostMapping("/{inventoryId}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN_GUDANG', 'OWNER')")
    public ResponseEntity<Map<String, Object>> adjustStock(@PathVariable UUID inventoryId,
                                                           @Valid @RequestBody AdjustStockRequest request) {
        InventoryResponse response = inventoryService.adjustStock(inventoryId, request);
        int previousStock = response.getCurrentStock() - request.getQuantity();
        return ResponseEntity.ok(Map.of(
                "inventoryId", response.getId(),
                "previousStock", previousStock,
                "newStock", response.getCurrentStock(),
                "message", "Stock adjusted successfully"
        ));
    }

    @GetMapping("/{inventoryId}/transactions")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_GUDANG')")
    public ResponseEntity<Page<StockMovementResponse>> getTransactionHistory(@PathVariable UUID inventoryId,
                                                                              Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getTransactionHistory(inventoryId, pageable));
    }
}
