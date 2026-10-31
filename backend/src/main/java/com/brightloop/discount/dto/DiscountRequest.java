package com.brightloop.discount.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    private String code;

    @NotBlank(message = "Discount name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Discount type is required (PERCENTAGE or FIXED_AMOUNT)")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
    private BigDecimal value;

    private BigDecimal minOrderAmount;

    private BigDecimal maxDiscountAmount;

    private String appliesTo; // ENTIRE_ORDER, SPECIFIC_PRODUCTS, SPECIFIC_PLANS

    private Integer usageLimit;

    private LocalDate validFrom;

    private LocalDate validUntil;

    private Boolean isActive;

    private List<UUID> productIds;

    private List<UUID> planIds;
}
