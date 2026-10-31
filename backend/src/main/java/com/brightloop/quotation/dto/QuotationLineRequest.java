package com.brightloop.quotation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class QuotationLineRequest {
    private UUID productId;
    private UUID variantId;
    private UUID planId;
    private String description;

    @NotNull
    @Builder.Default
    private Integer quantity = 1;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitPrice;

    private BigDecimal taxRate;
    private BigDecimal discountAmount;
}
