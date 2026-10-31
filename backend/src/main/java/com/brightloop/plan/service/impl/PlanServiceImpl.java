package com.brightloop.plan.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.plan.dto.PlanProductDto;
import com.brightloop.plan.dto.PlanRequest;
import com.brightloop.plan.dto.PlanResponse;
import com.brightloop.plan.entity.Plan;
import com.brightloop.plan.entity.PlanProduct;
import com.brightloop.plan.repo.PlanProductRepository;
import com.brightloop.plan.repo.PlanRepository;
import com.brightloop.plan.service.PlanService;
import com.brightloop.product.entity.Product;
import com.brightloop.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanProductRepository planProductRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlanResponse> listPlans(String search, String interval, Boolean isActive, Pageable pageable) {
        Page<Plan> page = planRepository.searchPlans(search, interval, isActive, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        String code = request.getPlanCode();
        if (code == null || code.isBlank()) {
            code = "PLAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        if (planRepository.existsByPlanCode(code)) {
            throw new BadRequestException("Plan with code " + code + " already exists");
        }

        if (request.getMaximumQuantity() != null && request.getMaximumQuantity() < request.getMinimumQuantity()) {
            throw new BadRequestException("Maximum quantity cannot be less than minimum quantity");
        }

        Plan plan = Plan.builder()
                .planCode(code)
                .name(request.getName())
                .description(request.getDescription())
                .billingInterval(request.getBillingInterval().toUpperCase())
                .intervalCount(request.getIntervalCount())
                .minimumQuantity(request.getMinimumQuantity())
                .maximumQuantity(request.getMaximumQuantity())
                .basePrice(request.getBasePrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .availableFrom(request.getAvailableFrom())
                .availableUntil(request.getAvailableUntil())
                .autoClose(request.getAutoClose() != null ? request.getAutoClose() : false)
                .allowEarlyClose(request.getAllowEarlyClose() != null ? request.getAllowEarlyClose() : true)
                .allowPause(request.getAllowPause() != null ? request.getAllowPause() : true)
                .allowRenewal(request.getAllowRenewal() != null ? request.getAllowRenewal() : true)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .planProducts(new ArrayList<>())
                .build();

        if (request.getProducts() != null) {
            for (PlanRequest.PlanProductItem item : request.getProducts()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
                PlanProduct pp = PlanProduct.builder()
                        .plan(plan)
                        .product(product)
                        .defaultQuantity(item.getDefaultQuantity() != null ? item.getDefaultQuantity() : 1)
                        .isMandatory(item.getIsMandatory() != null ? item.getIsMandatory() : true)
                        .build();
                plan.getPlanProducts().add(pp);
            }
        }

        return mapToResponse(planRepository.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(UUID id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
        return mapToResponse(plan);
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(UUID id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setBillingInterval(request.getBillingInterval().toUpperCase());
        plan.setIntervalCount(request.getIntervalCount());
        plan.setMinimumQuantity(request.getMinimumQuantity());
        plan.setMaximumQuantity(request.getMaximumQuantity());
        plan.setBasePrice(request.getBasePrice());
        if (request.getCurrency() != null) plan.setCurrency(request.getCurrency());
        plan.setAvailableFrom(request.getAvailableFrom());
        plan.setAvailableUntil(request.getAvailableUntil());
        if (request.getAutoClose() != null) plan.setAutoClose(request.getAutoClose());
        if (request.getAllowEarlyClose() != null) plan.setAllowEarlyClose(request.getAllowEarlyClose());
        if (request.getAllowPause() != null) plan.setAllowPause(request.getAllowPause());
        if (request.getAllowRenewal() != null) plan.setAllowRenewal(request.getAllowRenewal());
        if (request.getIsActive() != null) plan.setIsActive(request.getIsActive());

        return mapToResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void deletePlan(UUID id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
        plan.setIsActive(false);
        planRepository.save(plan);
    }

    private PlanResponse mapToResponse(Plan plan) {
        List<PlanProductDto> productDtos = new ArrayList<>();
        if (plan.getPlanProducts() != null) {
            for (PlanProduct pp : plan.getPlanProducts()) {
                productDtos.add(PlanProductDto.builder()
                        .id(pp.getId())
                        .productId(pp.getProduct().getId())
                        .productName(pp.getProduct().getName())
                        .productCode(pp.getProduct().getProductCode())
                        .defaultQuantity(pp.getDefaultQuantity())
                        .isMandatory(pp.getIsMandatory())
                        .build());
            }
        }

        return PlanResponse.builder()
                .id(plan.getId())
                .planCode(plan.getPlanCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .billingInterval(plan.getBillingInterval())
                .intervalCount(plan.getIntervalCount())
                .minimumQuantity(plan.getMinimumQuantity())
                .maximumQuantity(plan.getMaximumQuantity())
                .basePrice(plan.getBasePrice())
                .currency(plan.getCurrency())
                .availableFrom(plan.getAvailableFrom())
                .availableUntil(plan.getAvailableUntil())
                .autoClose(plan.getAutoClose())
                .allowEarlyClose(plan.getAllowEarlyClose())
                .allowPause(plan.getAllowPause())
                .allowRenewal(plan.getAllowRenewal())
                .isActive(plan.getIsActive())
                .products(productDtos)
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
