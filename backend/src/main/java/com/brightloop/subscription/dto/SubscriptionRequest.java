package com.brightloop.subscription.dto;

import jakarta.validation.Valid;
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
public class SubscriptionRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    private UUID quotationId;

    private String status; // DRAFT, ACTIVE, etc. Defaults to DRAFT

    private String billingInterval; // If null, inherits from plan

    private Integer billingIntervalCount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal unitPrice;

    @Builder.Default
    private Integer quantity = 1;

    private String paymentTerms;

    private String discountCode;

    @Valid
    private List<SubscriptionLineRequest> lines;
}
