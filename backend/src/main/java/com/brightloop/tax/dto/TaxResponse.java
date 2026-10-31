package com.brightloop.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxResponse {
    private UUID id;
    private String name;
    private String taxType;
    private BigDecimal rate;
    private String country;
    private String state;
    private Boolean isCompound;
    private Boolean isActive;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Instant createdAt;
}
