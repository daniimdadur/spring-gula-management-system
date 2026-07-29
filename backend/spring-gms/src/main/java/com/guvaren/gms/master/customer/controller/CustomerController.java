package com.guvaren.gms.master.customer.controller;

import com.guvaren.gms.master.customer.dto.request.CustomerRequest;
import com.guvaren.gms.master.customer.dto.response.CustomerResponse;
import com.guvaren.gms.master.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PENJUALAN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", response.getId(), "message", "Customer registered successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PENJUALAN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> updateCustomer(@PathVariable UUID id,
                                                              @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(Map.of("id", response.getId(), "message", "Customer updated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_PENJUALAN')")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(@RequestParam(required = false) String q,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN_PENJUALAN')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }
}
