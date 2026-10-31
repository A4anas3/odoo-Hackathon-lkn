package com.brightloop.quotation.dto;

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
public class QuotationRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private UUID templateId;

    private LocalDate orderDate;

    private LocalDate validUntil;

    private String termsAndConditions;

    private String notes;

    private String discountCode;

    @NotEmpty(message = "Quotation must have at least one line item")
    @Valid
    private List<QuotationLineRequest> lines;
}
