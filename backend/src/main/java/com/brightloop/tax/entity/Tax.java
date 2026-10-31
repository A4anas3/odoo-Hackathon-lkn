package com.brightloop.tax.entity;

import com.brightloop.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "taxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tax extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "tax_type", nullable = false, length = 30)
    private String taxType; // GST, CGST, SGST, IGST, OTHER

    @Column(name = "rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal rate; // e.g. 0.0900 for 9%

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "India";

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "is_compound", nullable = false)
    @Builder.Default
    private Boolean isCompound = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;
}
