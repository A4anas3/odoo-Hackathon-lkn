package com.brightloop.plan.entity;

import com.brightloop.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    @Column(name = "plan_code", unique = true, nullable = false, length = 80)
    private String planCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "billing_interval", nullable = false, length = 30)
    private String billingInterval; // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "interval_count", nullable = false)
    @Builder.Default
    private Integer intervalCount = 1;

    @Column(name = "minimum_quantity", nullable = false)
    @Builder.Default
    private Integer minimumQuantity = 1;

    @Column(name = "maximum_quantity")
    private Integer maximumQuantity;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "available_until")
    private LocalDate availableUntil;

    @Column(name = "auto_close", nullable = false)
    @Builder.Default
    private Boolean autoClose = false;

    @Column(name = "allow_early_close", nullable = false)
    @Builder.Default
    private Boolean allowEarlyClose = true;

    @Column(name = "allow_pause", nullable = false)
    @Builder.Default
    private Boolean allowPause = true;

    @Column(name = "allow_renewal", nullable = false)
    @Builder.Default
    private Boolean allowRenewal = true;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlanProduct> planProducts = new ArrayList<>();

    public String getCode() {
        return planCode;
    }

    public BigDecimal getPrice() {
        return basePrice;
    }

    public Integer getBillingIntervalCount() {
        return intervalCount != null ? intervalCount : 1;
    }
}
