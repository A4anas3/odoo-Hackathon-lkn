package com.brightloop.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private UUID id;
    private UUID userId;
    private String customerCode;
    private String customerType;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String billingAddress;
    private String shippingAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String gstin;
    private String taxRegistrationType;
    private String status;
    private String notes;
    private long activeSubscriptionsCount;
    private BigDecimal totalOutstanding;
    private Instant createdAt;
    private Instant updatedAt;
}
