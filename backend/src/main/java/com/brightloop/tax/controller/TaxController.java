package com.brightloop.tax.controller;

import com.brightloop.common.dto.ApiResponse;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.tax.dto.TaxCalculationResult;
import com.brightloop.tax.dto.TaxRequest;
import com.brightloop.tax.dto.TaxResponse;
import com.brightloop.tax.service.TaxCalculationService;
import com.brightloop.tax.service.TaxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/taxes")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;
    private final TaxCalculationService taxCalculationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaxResponse>>> listTaxes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        PageResponse<TaxResponse> result = taxService.listTaxes(search, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<TaxResponse>>> getActiveTaxes() {
        List<TaxResponse> list = taxService.listActiveTaxes();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<TaxCalculationResult>> calculateTax(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String customerState
    ) {
        TaxCalculationResult result = taxCalculationService.calculateTaxes(amount, customerState);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxResponse>> getTaxById(@PathVariable UUID id) {
        TaxResponse response = taxService.getTaxById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaxResponse>> createTax(@Valid @RequestBody TaxRequest request) {
        TaxResponse response = taxService.createTax(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaxResponse>> updateTax(
            @PathVariable UUID id,
            @Valid @RequestBody TaxRequest request
    ) {
        TaxResponse response = taxService.updateTax(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTax(@PathVariable UUID id) {
        taxService.deleteTax(id);
        return ResponseEntity.ok(ApiResponse.ok("Tax deactivated successfully", null));
    }
}
