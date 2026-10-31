package com.brightloop.customer.entity;

import com.brightloop.common.entity.BaseEntity;
import com.brightloop.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_code", unique = true, nullable = false, length = 50)
    private String customerCode;

    @Column(name = "customer_type", nullable = false, length = 30)
    @Builder.Default
    private String customerType = "BUSINESS"; // INDIVIDUAL, BUSINESS

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "contact_name", nullable = false, length = 150)
    private String contactName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "India";

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "tax_registration_type", length = 50)
    private String taxRegistrationType;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, ARCHIVED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public String getName() {
        return (companyName != null && !companyName.trim().isEmpty()) ? companyName : contactName;
    }

    public String getAddress() {
        return billingAddress != null ? billingAddress : shippingAddress;
    }
}
