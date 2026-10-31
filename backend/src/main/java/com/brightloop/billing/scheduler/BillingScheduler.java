package com.brightloop.billing.scheduler;

import com.brightloop.billing.service.RecurringBillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final RecurringBillingService recurringBillingService;

    @Scheduled(cron = "${app.billing.cron:0 */5 * * * *}")
    public void scheduleRecurringBilling() {
        log.info("Scheduled task: Starting automated recurring billing check...");
        try {
            recurringBillingService.runBillingCycle(LocalDate.now());
        } catch (Exception e) {
            log.error("Scheduled billing execution encountered an error: {}", e.getMessage(), e);
        }
    }
}
