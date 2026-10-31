package com.brightloop.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private UUID subscriptionId;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private String paymentTerms;

    private String notes;

    private LocalDate billingPeriodStart;

    private LocalDate billingPeriodEnd;

    private String discountCode;

    @NotEmpty(message = "Invoice must have at least one line item")
    @Valid
    private List<InvoiceLineRequest> lines;
}
