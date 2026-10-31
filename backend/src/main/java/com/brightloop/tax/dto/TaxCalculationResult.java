package com.brightloop.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCalculationResult {

    private BigDecimal taxableAmount;
    private BigDecimal totalTax;
    private BigDecimal effectiveRate;
    private List<TaxDetail> breakdown;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaxDetail {
        private UUID taxId;
        private String name;
        private String taxType;
        private BigDecimal rate;
        private BigDecimal taxAmount;
    }
}
