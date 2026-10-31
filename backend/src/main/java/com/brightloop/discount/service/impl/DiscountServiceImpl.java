package com.brightloop.discount.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.BusinessRuleException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.discount.dto.DiscountCalculationResult;
import com.brightloop.discount.dto.DiscountRequest;
import com.brightloop.discount.dto.DiscountResponse;
import com.brightloop.discount.entity.Discount;
import com.brightloop.discount.entity.DiscountProduct;
import com.brightloop.discount.entity.DiscountUsage;
import com.brightloop.discount.repo.DiscountRepository;
import com.brightloop.discount.repo.DiscountUsageRepository;
import com.brightloop.discount.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountUsageRepository discountUsageRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscountResponse> listDiscounts(String search, String type, Boolean isActive, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanType = (type != null && !type.trim().isEmpty()) ? type.toUpperCase().trim() : null;
        Page<Discount> page = discountRepository.findWithFilters(cleanSearch, cleanType, isActive, pageable);
        List<DiscountResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<DiscountResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountById(UUID id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        return mapToResponse(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountByCode(String code) {
        Discount discount = discountRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Discount coupon not found: " + code));
        return mapToResponse(discount);
    }

    @Override
    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (discountRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Coupon code already exists: " + code);
        }

        Discount discount = Discount.builder()
                .code(code)
                .name(request.getName().trim())
                .discountType(request.getDiscountType().toUpperCase().trim())
                .value(request.getValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .appliesTo(request.getAppliesTo() != null ? request.getAppliesTo().toUpperCase().trim() : "ENTIRE_ORDER")
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .targetProducts(new ArrayList<>())
                .build();

        if (request.getProductIds() != null) {
            for (UUID pId : request.getProductIds()) {
                discount.getTargetProducts().add(DiscountProduct.builder()
                        .discount(discount)
                        .productId(pId)
                        .build());
            }
        }
        if (request.getPlanIds() != null) {
            for (UUID plId : request.getPlanIds()) {
                discount.getTargetProducts().add(DiscountProduct.builder()
                        .discount(discount)
                        .planId(plId)
                        .build());
            }
        }

        Discount saved = discountRepository.save(discount);
        log.info("Created discount coupon: {}", saved.getCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(UUID id, DiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        String code = request.getCode().trim().toUpperCase();
        if (!discount.getCode().equalsIgnoreCase(code) && discountRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Coupon code already in use: " + code);
        }

        discount.setCode(code);
        discount.setName(request.getName().trim());
        discount.setDiscountType(request.getDiscountType().toUpperCase().trim());
        discount.setValue(request.getValue());
        discount.setMinOrderAmount(request.getMinOrderAmount());
        discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getAppliesTo() != null) discount.setAppliesTo(request.getAppliesTo().toUpperCase().trim());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setValidFrom(request.getValidFrom());
        discount.setValidUntil(request.getValidUntil());
        if (request.getIsActive() != null) discount.setIsActive(request.getIsActive());

        Discount updated = discountRepository.save(discount);
        log.info("Updated discount id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDiscount(UUID id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        discount.setIsActive(false);
        discountRepository.save(discount);
        log.info("Soft-deleted (deactivated) discount id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountCalculationResult validateAndCalculate(String code, BigDecimal orderAmount, UUID customerId) {
        if (code == null || code.trim().isEmpty()) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("No coupon code provided")
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount != null ? orderAmount : BigDecimal.ZERO)
                    .build();
        }

        Discount discount = discountRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (discount == null) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("Invalid coupon code")
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (!discount.getIsActive()) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("Coupon is inactive")
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        LocalDate today = LocalDate.now();
        if (discount.getValidFrom() != null && today.isBefore(discount.getValidFrom())) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("Coupon is not yet valid")
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (discount.getValidUntil() != null && today.isAfter(discount.getValidUntil())) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("Coupon has expired")
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (discount.getUsageLimit() != null && discount.getUsageCount() >= discount.getUsageLimit()) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("Coupon usage limit reached")
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        if (orderAmount == null) {
            orderAmount = BigDecimal.ZERO;
        }

        if (discount.getMinOrderAmount() != null && orderAmount.compareTo(discount.getMinOrderAmount()) < 0) {
            return DiscountCalculationResult.builder()
                    .valid(false)
                    .message("Order amount is below the minimum required: ₹" + discount.getMinOrderAmount())
                    .calculatedDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(orderAmount)
                    .build();
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("PERCENTAGE".equalsIgnoreCase(discount.getDiscountType())) {
            discountAmount = orderAmount.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (discount.getMaxDiscountAmount() != null && discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
                discountAmount = discount.getMaxDiscountAmount();
            }
        } else {
            // FIXED_AMOUNT
            discountAmount = discount.getValue();
            if (discountAmount.compareTo(orderAmount) > 0) {
                discountAmount = orderAmount;
            }
        }

        BigDecimal finalAmount = orderAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return DiscountCalculationResult.builder()
                .valid(true)
                .message("Coupon applied successfully")
                .discountId(discount.getId())
                .code(discount.getCode())
                .discountType(discount.getDiscountType())
                .discountValue(discount.getValue())
                .calculatedDiscountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    @Override
    @Transactional
    public void recordUsage(UUID discountId, UUID customerId, UUID orderId, UUID subscriptionId, UUID invoiceId, BigDecimal amount) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found: " + discountId));

        discount.setUsageCount(discount.getUsageCount() + 1);
        discountRepository.save(discount);

        DiscountUsage usage = DiscountUsage.builder()
                .discount(discount)
                .customerId(customerId)
                .orderId(orderId)
                .subscriptionId(subscriptionId)
                .invoiceId(invoiceId)
                .discountAmount(amount)
                .build();

        discountUsageRepository.save(usage);
        log.info("Recorded usage for discount {} by customer {}", discount.getCode(), customerId);
    }

    private DiscountResponse mapToResponse(Discount discount) {
        List<UUID> productIds = discount.getTargetProducts() != null ?
                discount.getTargetProducts().stream()
                        .filter(tp -> tp.getProductId() != null)
                        .map(DiscountProduct::getProductId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        List<UUID> planIds = discount.getTargetProducts() != null ?
                discount.getTargetProducts().stream()
                        .filter(tp -> tp.getPlanId() != null)
                        .map(DiscountProduct::getPlanId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .name(discount.getName())
                .discountType(discount.getDiscountType())
                .value(discount.getValue())
                .minOrderAmount(discount.getMinOrderAmount())
                .maxDiscountAmount(discount.getMaxDiscountAmount())
                .appliesTo(discount.getAppliesTo())
                .usageLimit(discount.getUsageLimit())
                .usageCount(discount.getUsageCount())
                .validFrom(discount.getValidFrom())
                .validUntil(discount.getValidUntil())
                .isActive(discount.getIsActive())
                .productIds(productIds)
                .planIds(planIds)
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }
}
