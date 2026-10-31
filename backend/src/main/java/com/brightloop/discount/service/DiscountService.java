package com.brightloop.discount.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.discount.dto.DiscountCalculationResult;
import com.brightloop.discount.dto.DiscountRequest;
import com.brightloop.discount.dto.DiscountResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface DiscountService {
    PageResponse<DiscountResponse> listDiscounts(String search, String type, Boolean isActive, Pageable pageable);
    DiscountResponse getDiscountById(UUID id);
    DiscountResponse getDiscountByCode(String code);
    DiscountResponse createDiscount(DiscountRequest request);
    DiscountResponse updateDiscount(UUID id, DiscountRequest request);
    void deleteDiscount(UUID id);
    DiscountCalculationResult validateAndCalculate(String code, BigDecimal orderAmount, UUID customerId);
    void recordUsage(UUID discountId, UUID customerId, UUID orderId, UUID subscriptionId, UUID invoiceId, BigDecimal amount);
}
