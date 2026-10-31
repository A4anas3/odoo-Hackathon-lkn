package com.brightloop.report.dto;

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
public class CustomerRevenueMetric {
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalBilled;
    private BigDecimal totalPaid;
    private BigDecimal balanceDue;
    private long activeSubscriptions;
}
