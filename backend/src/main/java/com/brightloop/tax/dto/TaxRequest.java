package com.brightloop.tax.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRequest {

    @NotBlank(message = "Tax name is required")
    private String name;

    @NotBlank(message = "Tax type is required (CGST, SGST, IGST, GST, OTHER)")
    private String taxType;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rate cannot be negative")
    private BigDecimal rate;

    private String country;
    private String state;
    private Boolean isCompound;
    private Boolean isActive;
    private LocalDate validFrom;
    private LocalDate validUntil;
}
