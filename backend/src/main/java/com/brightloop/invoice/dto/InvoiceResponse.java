package com.brightloop.invoice.dto;

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
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerGstin;
    private UUID subscriptionId;
    private String subscriptionNumber;
    private String status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private String currency;
    private String notes;
    private String paymentTerms;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private String pdfPath;
    private List<InvoiceLineResponse> lines;
    private List<InvoiceTaxLineResponse> taxLines;
    private Instant createdAt;
    private Instant updatedAt;
}
