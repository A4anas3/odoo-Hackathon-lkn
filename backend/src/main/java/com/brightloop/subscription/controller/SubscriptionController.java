package com.brightloop.subscription.controller;

import com.brightloop.common.dto.ApiResponse;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.security.jwt.UserPrincipal;
import com.brightloop.subscription.dto.SubscriptionPlanChangeRequest;
import com.brightloop.subscription.dto.SubscriptionRequest;
import com.brightloop.subscription.dto.SubscriptionResponse;
import com.brightloop.subscription.dto.SubscriptionStatusChangeRequest;
import com.brightloop.subscription.service.SubscriptionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionResponse>>> listSubscriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID planId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        PageResponse<SubscriptionResponse> result = subscriptionService.listSubscriptions(search, status, customerId, planId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(@PathVariable UUID id) {
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        SubscriptionResponse response = subscriptionService.createSubscription(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/from-quotation/{quotationId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createFromQuotation(
            @PathVariable UUID quotationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        SubscriptionResponse response = subscriptionService.createFromQuotation(quotationId, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionStatusChangeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        SubscriptionResponse response = subscriptionService.changeStatus(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/change-plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> changePlan(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionPlanChangeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        SubscriptionResponse response = subscriptionService.changePlan(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") Integer intervals,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        SubscriptionResponse response = subscriptionService.renewSubscription(id, intervals, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(@PathVariable UUID id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(ApiResponse.ok("Subscription deleted successfully", null));
    }
}
