package com.brightloop.quotation.dto;

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
public class QuotationLineResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID variantId;
    private String variantName;
    private UUID planId;
    private String planName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
}
