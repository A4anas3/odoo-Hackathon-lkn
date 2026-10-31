package com.brightloop.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {
    private UUID id;
    private String planCode;
    private String name;
    private String description;
    private String billingInterval;
    private Integer intervalCount;
    private Integer minimumQuantity;
    private Integer maximumQuantity;
    private BigDecimal basePrice;
    private String currency;
    private LocalDate availableFrom;
    private LocalDate availableUntil;
    private Boolean autoClose;
    private Boolean allowEarlyClose;
    private Boolean allowPause;
    private Boolean allowRenewal;
    private Boolean isActive;
    private List<PlanProductDto> products;
    private Instant createdAt;
    private Instant updatedAt;
}
