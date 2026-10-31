package com.brightloop.tax.service;

import com.brightloop.tax.dto.TaxCalculationResult;
import com.brightloop.tax.entity.Tax;
import com.brightloop.tax.repo.TaxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationService {

    private final TaxRepository taxRepository;

    @Value("${app.company.state:Uttar Pradesh}")
    private String companyState;

    @Transactional(readOnly = true)
    public TaxCalculationResult calculateTaxes(BigDecimal taxableAmount, String customerState) {
        if (taxableAmount == null || taxableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return TaxCalculationResult.builder()
                    .taxableAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .totalTax(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .effectiveRate(BigDecimal.ZERO)
                    .breakdown(new ArrayList<>())
                    .build();
        }

        taxableAmount = taxableAmount.setScale(2, RoundingMode.HALF_UP);
        List<Tax> activeTaxes = taxRepository.findActiveTaxesOnDate(LocalDate.now());

        boolean isIntraState = customerState != null && customerState.trim().equalsIgnoreCase(companyState.trim());

        List<TaxCalculationResult.TaxDetail> details = new ArrayList<>();
        BigDecimal totalTax = BigDecimal.ZERO;

        if (isIntraState) {
            // Apply CGST + SGST
            for (Tax tax : activeTaxes) {
                if ("CGST".equalsIgnoreCase(tax.getTaxType()) || "SGST".equalsIgnoreCase(tax.getTaxType())) {
                    BigDecimal taxAmount = taxableAmount.multiply(tax.getRate()).setScale(2, RoundingMode.HALF_UP);
                    totalTax = totalTax.add(taxAmount);

                    details.add(TaxCalculationResult.TaxDetail.builder()
                            .taxId(tax.getId())
                            .name(tax.getName())
                            .taxType(tax.getTaxType())
                            .rate(tax.getRate())
                            .taxAmount(taxAmount)
                            .build());
                }
            }
        } else {
            // Inter-state supply: Apply IGST
            for (Tax tax : activeTaxes) {
                if ("IGST".equalsIgnoreCase(tax.getTaxType())) {
                    BigDecimal taxAmount = taxableAmount.multiply(tax.getRate()).setScale(2, RoundingMode.HALF_UP);
                    totalTax = totalTax.add(taxAmount);

                    details.add(TaxCalculationResult.TaxDetail.builder()
                            .taxId(tax.getId())
                            .name(tax.getName())
                            .taxType(tax.getTaxType())
                            .rate(tax.getRate())
                            .taxAmount(taxAmount)
                            .build());
                }
            }
        }

        // If no state specific rule found, fallback to generic active GST
        if (details.isEmpty()) {
            for (Tax tax : activeTaxes) {
                if ("GST".equalsIgnoreCase(tax.getTaxType())) {
                    BigDecimal taxAmount = taxableAmount.multiply(tax.getRate()).setScale(2, RoundingMode.HALF_UP);
                    totalTax = totalTax.add(taxAmount);

                    details.add(TaxCalculationResult.TaxDetail.builder()
                            .taxId(tax.getId())
                            .name(tax.getName())
                            .taxType(tax.getTaxType())
                            .rate(tax.getRate())
                            .taxAmount(taxAmount)
                            .build());
                }
            }
        }

        BigDecimal effectiveRate = taxableAmount.compareTo(BigDecimal.ZERO) > 0
                ? totalTax.divide(taxableAmount, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return TaxCalculationResult.builder()
                .taxableAmount(taxableAmount)
                .totalTax(totalTax.setScale(2, RoundingMode.HALF_UP))
                .effectiveRate(effectiveRate)
                .breakdown(details)
                .build();
    }
}
