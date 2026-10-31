package com.brightloop.discount.entity;

import com.brightloop.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "discount_type", nullable = false, length = 30)
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "minimum_purchase", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "maximum_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "applies_to", nullable = false, length = 30)
    @Builder.Default
    private String appliesTo = "ENTIRE_ORDER"; // ENTIRE_ORDER, SPECIFIC_PRODUCTS, SPECIFIC_PLANS

    @Column(name = "maximum_uses")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DiscountProduct> targetProducts = new ArrayList<>();
}
