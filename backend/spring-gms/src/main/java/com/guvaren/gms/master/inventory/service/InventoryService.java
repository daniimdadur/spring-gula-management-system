package com.guvaren.gms.master.inventory.service;

import com.guvaren.gms.common.event.InventoryUpdatedEvent;
import com.guvaren.gms.common.event.LowStockEvent;
import com.guvaren.gms.master.inventory.dto.request.AdjustStockRequest;
import com.guvaren.gms.master.inventory.dto.request.StockInRequest;
import com.guvaren.gms.master.inventory.dto.request.StockOutRequest;
import com.guvaren.gms.master.inventory.dto.response.InventoryResponse;
import com.guvaren.gms.master.inventory.dto.response.StockMovementResponse;
import com.guvaren.gms.master.inventory.entity.Inventory;
import com.guvaren.gms.master.inventory.entity.MovementType;
import com.guvaren.gms.master.inventory.entity.ReferenceType;
import com.guvaren.gms.master.inventory.entity.StockMovement;
import com.guvaren.gms.master.inventory.repository.InventoryRepository;
import com.guvaren.gms.master.inventory.repository.StockMovementRepository;
import com.guvaren.gms.master.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product id: " + productId));
        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse stockIn(UUID inventoryId, StockInRequest request) {
        Inventory inventory = findInventoryById(inventoryId);
        int previousStock = inventory.getCurrentStock();
        inventory.setCurrentStock(previousStock + request.getQuantity());

        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.IN)
                .quantity(request.getQuantity())
                .referenceType(ReferenceType.MANUAL)
                .notes(request.getNotes())
                .build();
        stockMovementRepository.save(movement);
        inventoryRepository.save(inventory);

        publishInventoryUpdatedEvent(inventory, previousStock, MovementType.IN, request.getQuantity(), ReferenceType.MANUAL, null);
        checkAndPublishLowStock(inventory);

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse stockOut(UUID inventoryId, StockOutRequest request) {
        Inventory inventory = findInventoryById(inventoryId);
        int previousStock = inventory.getCurrentStock();

        if (previousStock < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stok tidak mencukupi untuk melakukan transaksi");
        }

        inventory.setCurrentStock(previousStock - request.getQuantity());

        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.OUT)
                .quantity(request.getQuantity())
                .referenceType(ReferenceType.MANUAL)
                .notes(request.getNotes())
                .build();
        stockMovementRepository.save(movement);
        inventoryRepository.save(inventory);

        publishInventoryUpdatedEvent(inventory, previousStock, MovementType.OUT, -request.getQuantity(), ReferenceType.MANUAL, null);
        checkAndPublishLowStock(inventory);

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse adjustStock(UUID inventoryId, AdjustStockRequest request) {
        Inventory inventory = findInventoryById(inventoryId);
        int previousStock = inventory.getCurrentStock();
        int newStock = previousStock + request.getQuantity();

        if (newStock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stok tidak boleh negatif");
        }

        inventory.setCurrentStock(newStock);

        MovementType movementType = request.getQuantity() >= 0 ? MovementType.IN : MovementType.OUT;
        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(request.getQuantity())
                .referenceType(ReferenceType.ADJUSTMENT)
                .notes(request.getNotes())
                .build();
        stockMovementRepository.save(movement);
        inventoryRepository.save(inventory);

        publishInventoryUpdatedEvent(inventory, previousStock, movementType, request.getQuantity(), ReferenceType.ADJUSTMENT, null);
        checkAndPublishLowStock(inventory);

        return mapToResponse(inventory);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getTransactionHistory(UUID inventoryId, Pageable pageable) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found with id: " + inventoryId);
        }
        return stockMovementRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId, pageable)
                .map(this::mapToMovementResponse);
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryEntityByProductId(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product id: " + productId));
    }

    @Transactional(readOnly = true)
    public boolean hasSufficientStock(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product id: " + productId));
        return inventory.getCurrentStock() >= quantity;
    }

    @Transactional
    public void deductStockForOrder(UUID productId, int quantity, UUID orderId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product id: " + productId));

        int previousStock = inventory.getCurrentStock();
        if (previousStock < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stok tidak mencukupi untuk produk: " + productId);
        }

        inventory.setCurrentStock(previousStock - quantity);

        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.OUT)
                .quantity(quantity)
                .referenceType(ReferenceType.ORDER)
                .referenceId(orderId)
                .notes("Payment confirmed")
                .build();
        stockMovementRepository.save(movement);
        inventoryRepository.save(inventory);

        publishInventoryUpdatedEvent(inventory, previousStock, MovementType.OUT, -quantity, ReferenceType.ORDER, orderId);
        checkAndPublishLowStock(inventory);
    }

    @Transactional
    public void addStockForProduction(UUID productId, int quantity, UUID productionId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product id: " + productId));

        int previousStock = inventory.getCurrentStock();
        inventory.setCurrentStock(previousStock + quantity);

        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.IN)
                .quantity(quantity)
                .referenceType(ReferenceType.PRODUCTION)
                .referenceId(productionId)
                .notes("Production completed")
                .build();
        stockMovementRepository.save(movement);
        inventoryRepository.save(inventory);

        publishInventoryUpdatedEvent(inventory, previousStock, MovementType.IN, quantity, ReferenceType.PRODUCTION, productionId);
    }

    private void publishInventoryUpdatedEvent(Inventory inventory, int previousStock, MovementType transactionType,
                                              int changeQuantity, ReferenceType referenceType, UUID referenceId) {
        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .inventoryId(inventory.getId())
                .productId(inventory.getProduct().getId())
                .previousStock(previousStock)
                .currentStock(inventory.getCurrentStock())
                .minimumStock(inventory.getMinimumStock())
                .transactionType(transactionType)
                .changeQuantity(changeQuantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        eventPublisher.publishEvent(event);
    }

    private void checkAndPublishLowStock(Inventory inventory) {
        if (inventory.getCurrentStock() < inventory.getMinimumStock()) {
            LowStockEvent event = LowStockEvent.builder()
                    .inventoryId(inventory.getId())
                    .productId(inventory.getProduct().getId())
                    .productName(inventory.getProduct().getName())
                    .currentStock(inventory.getCurrentStock())
                    .minimumStock(inventory.getMinimumStock())
                    .build();
            eventPublisher.publishEvent(event);
        }
    }

    private Inventory findInventoryById(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found with id: " + id));
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .currentStock(inventory.getCurrentStock())
                .minimumStock(inventory.getMinimumStock())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private StockMovementResponse mapToMovementResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .notes(movement.getNotes())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
