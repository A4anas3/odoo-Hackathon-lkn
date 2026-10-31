package com.brightloop.plan.controller;

import com.brightloop.common.dto.ApiResponse;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.plan.dto.PlanRequest;
import com.brightloop.plan.dto.PlanResponse;
import com.brightloop.plan.service.PlanService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PlanResponse>>> listPlans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        PageResponse<PlanResponse> result = planService.listPlans(search, interval, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(
            @Valid @RequestBody PlanRequest request
    ) {
        PlanResponse result = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Plan created successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlanById(
            @PathVariable UUID id
    ) {
        PlanResponse result = planService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody PlanRequest request
    ) {
        PlanResponse result = planService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Plan updated successfully", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(
            @PathVariable UUID id
    ) {
        planService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.ok("Plan deactivated successfully", null));
    }
}
