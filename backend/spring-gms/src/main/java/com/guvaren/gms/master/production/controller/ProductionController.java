package com.guvaren.gms.master.production.controller;

import com.guvaren.gms.master.production.dto.request.ProductionRequest;
import com.guvaren.gms.master.production.dto.response.ProductionResponse;
import com.guvaren.gms.master.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/productions")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_GUDANG')")
    public ResponseEntity<Map<String, Object>> createProduction(@Valid @RequestBody ProductionRequest request) {
        ProductionResponse response = productionService.createProduction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "productionId", response.getId(),
                        "message", "Production record created, event published for inventory update."
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_GUDANG')")
    public ResponseEntity<Page<ProductionResponse>> getAllProductions(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(productionService.getAllProductions(productId, startDate, endDate, pageable));
    }
}
