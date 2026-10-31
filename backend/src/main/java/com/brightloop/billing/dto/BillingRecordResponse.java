package com.brightloop.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRecordResponse {
    private UUID id;
    private UUID subscriptionId;
    private String subscriptionNumber;
    private UUID invoiceId;
    private String invoiceNumber;
    private LocalDate billingDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal amount;
    private String status;
    private String errorMessage;
    private Instant createdAt;
}
