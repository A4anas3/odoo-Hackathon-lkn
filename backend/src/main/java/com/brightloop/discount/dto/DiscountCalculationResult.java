package com.brightloop.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCalculationResult {
    private boolean valid;
    private String message;
    private UUID discountId;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal calculatedDiscountAmount;
    private BigDecimal finalAmount;
}
