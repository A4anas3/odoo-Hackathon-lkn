package com.brightloop.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {

    @NotBlank(message = "Attribute name is required")
    private String attributeName;

    @NotBlank(message = "Attribute value is required")
    private String attributeValue;

    @NotNull(message = "Additional price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Additional price cannot be negative")
    private BigDecimal additionalPrice;

    private String sku;
    private Boolean isActive;
}
