package com.brightloop.quotation.dto;

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
public class QuotationResponse {
    private UUID id;
    private String quotationNumber;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private UUID templateId;
    private String templateName;
    private String status;
    private LocalDate orderDate;
    private LocalDate validUntil;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String termsAndConditions;
    private String notes;
    private List<QuotationLineResponse> lines;
    private Instant createdAt;
    private Instant updatedAt;
}
