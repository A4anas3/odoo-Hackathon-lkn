package com.brightloop.product.dto;

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
public class ProductVariantResponse {
    private UUID id;
    private UUID productId;
    private String attributeName;
    private String attributeValue;
    private BigDecimal additionalPrice;
    private String sku;
    private Boolean isActive;
}
