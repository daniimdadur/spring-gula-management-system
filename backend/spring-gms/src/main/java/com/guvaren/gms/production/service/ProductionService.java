package com.guvaren.gms.production.service;

import com.guvaren.gms.common.event.ProductionCompletedEvent;
import com.guvaren.gms.production.dto.request.ProductionRequest;
import com.guvaren.gms.production.dto.response.ProductionResponse;
import com.guvaren.gms.production.entity.Production;
import com.guvaren.gms.production.repository.ProductionRepository;
import com.guvaren.gms.product.entity.Product;
import com.guvaren.gms.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProductionResponse createProduction(ProductionRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + request.getProductId()));

        Production production = Production.builder()
                .product(product)
                .quantity(request.getQuantity())
                .productionDate(request.getProductionDate())
                .notes(request.getNotes())
                .build();

        production = productionRepository.save(production);

        ProductionCompletedEvent event = ProductionCompletedEvent.builder()
                .productionId(production.getId())
                .productId(product.getId())
                .quantity(production.getQuantity())
                .productionDate(production.getProductionDate())
                .notes(production.getNotes())
                .build();
        eventPublisher.publishEvent(event);

        return mapToResponse(production);
    }

    @Transactional(readOnly = true)
    public Page<ProductionResponse> getAllProductions(UUID productId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return productionRepository.findByFilters(productId, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    private ProductionResponse mapToResponse(Production production) {
        return ProductionResponse.builder()
                .id(production.getId())
                .productId(production.getProduct().getId())
                .productName(production.getProduct().getName())
                .quantity(production.getQuantity())
                .productionDate(production.getProductionDate())
                .notes(production.getNotes())
                .createdAt(production.getCreatedAt())
                .build();
    }
}
