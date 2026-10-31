package com.brightloop.quotation.controller;

import com.brightloop.common.dto.ApiResponse;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.quotation.dto.QuotationRequest;
import com.brightloop.quotation.dto.QuotationResponse;
import com.brightloop.quotation.service.QuotationService;
import com.brightloop.security.jwt.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponse>>> listQuotations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        PageResponse<QuotationResponse> result = quotationService.listQuotations(search, status, customerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotationById(@PathVariable UUID id) {
        QuotationResponse response = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuotationResponse>> createQuotation(
            @Valid @RequestBody QuotationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        QuotationResponse response = quotationService.createQuotation(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotation(
            @PathVariable UUID id,
            @Valid @RequestBody QuotationRequest request
    ) {
        QuotationResponse response = quotationService.updateQuotation(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        QuotationResponse response = quotationService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuotation(@PathVariable UUID id) {
        quotationService.deleteQuotation(id);
        return ResponseEntity.ok(ApiResponse.ok("Quotation deleted successfully", null));
    }
}
