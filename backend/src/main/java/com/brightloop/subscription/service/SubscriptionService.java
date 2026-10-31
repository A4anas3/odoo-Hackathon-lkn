package com.brightloop.subscription.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.subscription.dto.SubscriptionPlanChangeRequest;
import com.brightloop.subscription.dto.SubscriptionRequest;
import com.brightloop.subscription.dto.SubscriptionResponse;
import com.brightloop.subscription.dto.SubscriptionStatusChangeRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SubscriptionService {
    PageResponse<SubscriptionResponse> listSubscriptions(String search, String status, UUID customerId, UUID planId, Pageable pageable);
    SubscriptionResponse getSubscriptionById(UUID id);
    SubscriptionResponse createSubscription(SubscriptionRequest request, UUID currentUserId);
    SubscriptionResponse createFromQuotation(UUID quotationId, UUID currentUserId);
    SubscriptionResponse changeStatus(UUID id, SubscriptionStatusChangeRequest request, UUID currentUserId);
    SubscriptionResponse changePlan(UUID id, SubscriptionPlanChangeRequest request, UUID currentUserId);
    SubscriptionResponse renewSubscription(UUID id, Integer additionalIntervals, UUID currentUserId);
    void deleteSubscription(UUID id);
}
