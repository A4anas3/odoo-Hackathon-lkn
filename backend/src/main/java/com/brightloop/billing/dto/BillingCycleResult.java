package com.brightloop.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingCycleResult {
    private LocalDate executionDate;
    private int subscriptionsProcessed;
    private int invoicesGenerated;
    private int expiredSubscriptions;
    private int failedCount;
    private List<String> messages;
}
