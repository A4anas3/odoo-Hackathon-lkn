package com.brightloop.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAllocationResponse {
    private UUID id;
    private UUID invoiceId;
    private String invoiceNumber;
    private BigDecimal allocatedAmount;
    private Instant allocatedAt;
}
