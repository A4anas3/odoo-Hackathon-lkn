package com.brightloop.product.dto;

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
public class ProductResponse {
    private UUID id;
    private String productCode;
    private String name;
    private String description;
    private String productType;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private String currency;
    private String notes;
    private Boolean isActive;
    private List<ProductVariantResponse> variants;
    private Instant createdAt;
    private Instant updatedAt;
}
