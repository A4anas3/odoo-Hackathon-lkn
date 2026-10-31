package com.brightloop.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PlanRequest {

    private String planCode;

    @NotBlank(message = "Plan name is required")
    private String name;

    private String description;

    @NotBlank(message = "Billing interval is required (DAILY, WEEKLY, MONTHLY, YEARLY)")
    private String billingInterval;

    @NotNull(message = "Interval count is required")
    @Min(value = 1, message = "Interval count must be at least 1")
    private Integer intervalCount;

    @NotNull(message = "Minimum quantity is required")
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minimumQuantity;

    private Integer maximumQuantity;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price cannot be negative")
    private BigDecimal basePrice;

    private String currency;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    private Boolean autoClose;
    private Boolean allowEarlyClose;
    private Boolean allowPause;
    private Boolean allowRenewal;
    private Boolean isActive;

    private List<PlanProductItem> products;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanProductItem {
        private UUID productId;
        private Integer defaultQuantity;
        private Boolean isMandatory;
    }
}
