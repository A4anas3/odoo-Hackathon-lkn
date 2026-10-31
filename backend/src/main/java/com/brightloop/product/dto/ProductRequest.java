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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    private String productCode;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;
    private String productType; // SERVICE, SUBSCRIPTION, ADDON

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Sale price cannot be negative")
    private BigDecimal salePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    private String currency;
    private String notes;
    private Boolean isActive;
    private List<ProductVariantRequest> variants;
}
