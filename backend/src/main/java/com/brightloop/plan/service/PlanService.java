package com.brightloop.plan.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.plan.dto.PlanRequest;
import com.brightloop.plan.dto.PlanResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PlanService {

    PageResponse<PlanResponse> listPlans(String search, String interval, Boolean isActive, Pageable pageable);

    PlanResponse createPlan(PlanRequest request);

    PlanResponse getPlanById(UUID id);

    PlanResponse updatePlan(UUID id, PlanRequest request);

    void deletePlan(UUID id);
}
