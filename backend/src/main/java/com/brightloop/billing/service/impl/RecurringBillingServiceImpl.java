package com.brightloop.billing.service.impl;

import com.brightloop.billing.dto.BillingCycleResult;
import com.brightloop.billing.dto.BillingRecordResponse;
import com.brightloop.billing.entity.BillingRecord;
import com.brightloop.billing.repo.BillingRecordRepository;
import com.brightloop.billing.service.RecurringBillingService;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.invoice.entity.Invoice;
import com.brightloop.invoice.service.InvoiceService;
import com.brightloop.subscription.entity.Subscription;
import com.brightloop.subscription.entity.SubscriptionHistory;
import com.brightloop.subscription.repo.SubscriptionHistoryRepository;
import com.brightloop.subscription.repo.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringBillingServiceImpl implements RecurringBillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public BillingCycleResult runBillingCycle(LocalDate referenceDate) {
        LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();
        log.info("Starting recurring billing cycle for date: {}", today);

        List<Subscription> dueSubscriptions = subscriptionRepository.findDueSubscriptions(today);
        log.info("Found {} due subscriptions to process", dueSubscriptions.size());

        int processed = 0;
        int generated = 0;
        int expired = 0;
        int failed = 0;
        List<String> messages = new ArrayList<>();

        for (Subscription subscription : dueSubscriptions) {
            processed++;
            try {
                // Check if expired
                if (subscription.getEndDate() != null && today.isAfter(subscription.getEndDate())) {
                    subscription.setStatus("EXPIRED");
                    subscriptionRepository.save(subscription);

                    SubscriptionHistory history = SubscriptionHistory.builder()
                            .subscription(subscription)
                            .fromStatus("ACTIVE")
                            .toStatus("EXPIRED")
                            .reason("Subscription end date reached: " + subscription.getEndDate())
                            .build();
                    subscriptionHistoryRepository.save(history);

                    BillingRecord record = BillingRecord.builder()
                            .subscription(subscription)
                            .billingDate(today)
                            .periodStart(subscription.getLastBillingDate() != null ? subscription.getLastBillingDate() : subscription.getStartDate())
                            .periodEnd(subscription.getEndDate())
                            .amount(subscription.getTotalAmount())
                            .status("SKIPPED")
                            .errorMessage("Subscription expired on " + subscription.getEndDate())
                            .build();
                    billingRecordRepository.save(record);

                    expired++;
                    messages.add("Subscription " + subscription.getSubscriptionNumber() + " expired");
                    continue;
                }

                // Normal recurring billing cycle
                LocalDate periodStart = subscription.getLastBillingDate() != null ?
                        subscription.getLastBillingDate() : subscription.getStartDate();
                LocalDate periodEnd = subscription.getNextBillingDate();

                Invoice invoice = invoiceService.createRecurringInvoice(subscription, periodStart, periodEnd);

                // Advance subscription dates
                subscription.setLastBillingDate(today);
                LocalDate nextBilling = advanceDate(today, subscription.getBillingInterval(), subscription.getBillingIntervalCount());
                subscription.setNextBillingDate(nextBilling);
                subscriptionRepository.save(subscription);

                BillingRecord record = BillingRecord.builder()
                        .subscription(subscription)
                        .invoice(invoice)
                        .billingDate(today)
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .amount(invoice.getTotalAmount())
                        .status("SUCCESS")
                        .build();
                billingRecordRepository.save(record);

                generated++;
                messages.add("Billed " + subscription.getSubscriptionNumber() + " -> Invoice " + invoice.getInvoiceNumber());
            } catch (Exception e) {
                log.error("Failed to bill subscription {}: {}", subscription.getSubscriptionNumber(), e.getMessage(), e);
                failed++;
                messages.add("Failed " + subscription.getSubscriptionNumber() + ": " + e.getMessage());

                BillingRecord record = BillingRecord.builder()
                        .subscription(subscription)
                        .billingDate(today)
                        .periodStart(today)
                        .periodEnd(today)
                        .amount(subscription.getTotalAmount())
                        .status("FAILED")
                        .errorMessage(e.getMessage())
                        .build();
                billingRecordRepository.save(record);
            }
        }

        log.info("Finished billing cycle. Processed: {}, Generated: {}, Expired: {}, Failed: {}",
                processed, generated, expired, failed);

        return BillingCycleResult.builder()
                .executionDate(today)
                .subscriptionsProcessed(processed)
                .invoicesGenerated(generated)
                .expiredSubscriptions(expired)
                .failedCount(failed)
                .messages(messages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BillingRecordResponse> listBillingRecords(UUID subscriptionId, String status, Pageable pageable) {
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.toUpperCase().trim() : null;
        Page<BillingRecord> page = billingRecordRepository.findWithFilters(subscriptionId, cleanStatus, pageable);
        List<BillingRecordResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<BillingRecordResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    private LocalDate advanceDate(LocalDate base, String interval, int count) {
        if (count <= 0) count = 1;
        return switch (interval.toUpperCase()) {
            case "DAILY" -> base.plusDays(count);
            case "WEEKLY" -> base.plusWeeks(count);
            case "MONTHLY" -> base.plusMonths(count);
            case "QUARTERLY" -> base.plusMonths(3L * count);
            case "YEARLY" -> base.plusYears(count);
            default -> base.plusMonths(count);
        };
    }

    private BillingRecordResponse mapToResponse(BillingRecord r) {
        return BillingRecordResponse.builder()
                .id(r.getId())
                .subscriptionId(r.getSubscription() != null ? r.getSubscription().getId() : null)
                .subscriptionNumber(r.getSubscription() != null ? r.getSubscription().getSubscriptionNumber() : null)
                .invoiceId(r.getInvoice() != null ? r.getInvoice().getId() : null)
                .invoiceNumber(r.getInvoice() != null ? r.getInvoice().getInvoiceNumber() : null)
                .billingDate(r.getBillingDate())
                .periodStart(r.getPeriodStart())
                .periodEnd(r.getPeriodEnd())
                .amount(r.getAmount())
                .status(r.getStatus())
                .errorMessage(r.getErrorMessage())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
