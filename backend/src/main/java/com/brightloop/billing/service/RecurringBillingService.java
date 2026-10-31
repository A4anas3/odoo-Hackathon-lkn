package com.brightloop.billing.service;

import com.brightloop.billing.dto.BillingCycleResult;
import com.brightloop.billing.dto.BillingRecordResponse;
import com.brightloop.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface RecurringBillingService {
    BillingCycleResult runBillingCycle(LocalDate referenceDate);
    PageResponse<BillingRecordResponse> listBillingRecords(UUID subscriptionId, String status, Pageable pageable);
}
