package com.brightloop.discount.dto;

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
public class DiscountResponse {
    private UUID id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private String appliesTo;
    private Integer usageLimit;
    private Integer usageCount;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean isActive;
    private List<UUID> productIds;
    private List<UUID> planIds;
    private Instant createdAt;
    private Instant updatedAt;
}
