package com.brightloop.subscription.dto;

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
public class SubscriptionResponse {
    private UUID id;
    private String subscriptionNumber;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private UUID planId;
    private String planName;
    private String planCode;
    private UUID quotationId;
    private String status;
    private String billingInterval;
    private Integer billingIntervalCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextBillingDate;
    private LocalDate lastBillingDate;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String autoCloseReason;
    private String paymentTerms;
    private List<SubscriptionLineResponse> lines;
    private List<SubscriptionHistoryResponse> history;
    private Instant createdAt;
    private Instant updatedAt;
}
