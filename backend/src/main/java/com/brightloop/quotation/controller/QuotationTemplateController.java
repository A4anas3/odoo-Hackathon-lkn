package com.brightloop.quotation.controller;

import com.brightloop.common.dto.ApiResponse;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.quotation.dto.QuotationTemplateRequest;
import com.brightloop.quotation.dto.QuotationTemplateResponse;
import com.brightloop.quotation.service.QuotationService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotation-templates")
@RequiredArgsConstructor
public class QuotationTemplateController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<QuotationTemplateResponse>>> listTemplates(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        PageResponse<QuotationTemplateResponse> result = quotationService.listTemplates(search, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<QuotationTemplateResponse>>> getActiveTemplates() {
        List<QuotationTemplateResponse> list = quotationService.listActiveTemplates();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationTemplateResponse>> getTemplateById(@PathVariable UUID id) {
        QuotationTemplateResponse response = quotationService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationTemplateResponse>> createTemplate(@Valid @RequestBody QuotationTemplateRequest request) {
        QuotationTemplateResponse response = quotationService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuotationTemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody QuotationTemplateRequest request
    ) {
        QuotationTemplateResponse response = quotationService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        quotationService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.ok("Quotation template deactivated successfully", null));
    }
}
