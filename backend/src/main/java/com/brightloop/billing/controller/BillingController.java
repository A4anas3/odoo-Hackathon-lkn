package com.brightloop.billing.controller;

import com.brightloop.billing.dto.BillingCycleResult;
import com.brightloop.billing.dto.BillingRecordResponse;
import com.brightloop.billing.service.RecurringBillingService;
import com.brightloop.common.dto.ApiResponse;
import com.brightloop.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final RecurringBillingService recurringBillingService;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BillingCycleResult>> triggerBillingCycle(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate runDate = date != null ? date : LocalDate.now();
        BillingCycleResult result = recurringBillingService.runBillingCycle(runDate);
        return ResponseEntity.ok(ApiResponse.ok("Billing cycle completed successfully", result));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<PageResponse<BillingRecordResponse>>> listBillingRecords(
            @RequestParam(required = false) UUID subscriptionId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        PageResponse<BillingRecordResponse> result = recurringBillingService.listBillingRecords(subscriptionId, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
