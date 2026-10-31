package com.brightloop.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID id;
    private String paymentNumber;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String paymentMethod;
    private Instant paymentDate;
    private String status;
    private String referenceNumber;
    private String notes;
    private List<PaymentAllocationResponse> allocations;
    private Instant createdAt;
    private Instant updatedAt;
}
