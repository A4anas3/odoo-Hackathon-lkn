package com.brightloop.subscription.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.BusinessRuleException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.common.service.SequenceGeneratorService;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repo.CustomerRepository;
import com.brightloop.discount.dto.DiscountCalculationResult;
import com.brightloop.discount.service.DiscountService;
import com.brightloop.plan.entity.Plan;
import com.brightloop.plan.repo.PlanRepository;
import com.brightloop.product.entity.Product;
import com.brightloop.product.repo.ProductRepository;
import com.brightloop.quotation.entity.Quotation;
import com.brightloop.quotation.entity.QuotationLine;
import com.brightloop.quotation.repo.QuotationRepository;
import com.brightloop.subscription.dto.SubscriptionHistoryResponse;
import com.brightloop.subscription.dto.SubscriptionLineRequest;
import com.brightloop.subscription.dto.SubscriptionLineResponse;
import com.brightloop.subscription.dto.SubscriptionPlanChangeRequest;
import com.brightloop.subscription.dto.SubscriptionRequest;
import com.brightloop.subscription.dto.SubscriptionResponse;
import com.brightloop.subscription.dto.SubscriptionStatusChangeRequest;
import com.brightloop.subscription.entity.Subscription;
import com.brightloop.subscription.entity.SubscriptionHistory;
import com.brightloop.subscription.entity.SubscriptionLine;
import com.brightloop.subscription.repo.SubscriptionHistoryRepository;
import com.brightloop.subscription.repo.SubscriptionRepository;
import com.brightloop.subscription.service.SubscriptionService;
import com.brightloop.tax.dto.TaxCalculationResult;
import com.brightloop.tax.service.TaxCalculationService;
import com.brightloop.user.entity.User;
import com.brightloop.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final QuotationRepository quotationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final TaxCalculationService taxCalculationService;
    private final DiscountService discountService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubscriptionResponse> listSubscriptions(
            String search, String status, UUID customerId, UUID planId, Pageable pageable
    ) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.toUpperCase().trim() : null;

        Page<Subscription> page = subscriptionRepository.findWithFilters(cleanSearch, cleanStatus, customerId, planId, pageable);
        List<SubscriptionResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<SubscriptionResponse>builder()
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

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        Subscription subscription = subscriptionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        return mapToResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request, UUID currentUserId) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + request.getPlanId()));

        String subNumber = sequenceGeneratorService.generateSubscriptionNumber();
        String interval = request.getBillingInterval() != null ? request.getBillingInterval().toUpperCase().trim() : plan.getBillingInterval();
        int intervalCount = request.getBillingIntervalCount() != null ? request.getBillingIntervalCount() : plan.getBillingIntervalCount();
        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : plan.getPrice();
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate nextBillingDate = calculateNextBillingDate(startDate, interval, intervalCount);

        String initialStatus = request.getStatus() != null ? request.getStatus().toUpperCase().trim() : "ACTIVE";

        Subscription subscription = Subscription.builder()
                .subscriptionNumber(subNumber)
                .customer(customer)
                .plan(plan)
                .quotationId(request.getQuotationId())
                .status(initialStatus)
                .billingInterval(interval)
                .billingIntervalCount(intervalCount)
                .startDate(startDate)
                .endDate(request.getEndDate())
                .nextBillingDate(nextBillingDate)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : "Immediate")
                .lines(new ArrayList<>())
                .history(new LinkedHashSet<>())
                .build();

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

        if (request.getLines() != null && !request.getLines().isEmpty()) {
            for (SubscriptionLineRequest lineReq : request.getLines()) {
                BigDecimal qty = BigDecimal.valueOf(lineReq.getQuantity());
                BigDecimal linePrice = lineReq.getUnitPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
                BigDecimal discount = lineReq.getDiscountAmount() != null ? lineReq.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal lineTotal = linePrice.subtract(discount);
                if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

                SubscriptionLine line = SubscriptionLine.builder()
                        .subscription(subscription)
                        .productId(lineReq.getProductId())
                        .variantId(lineReq.getVariantId())
                        .description(lineReq.getDescription())
                        .quantity(lineReq.getQuantity())
                        .unitPrice(lineReq.getUnitPrice())
                        .taxRate(lineReq.getTaxRate() != null ? lineReq.getTaxRate() : BigDecimal.ZERO)
                        .discountAmount(discount)
                        .lineTotal(lineTotal)
                        .build();

                subscription.getLines().add(line);
                subtotal = subtotal.add(lineTotal);
            }
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
            DiscountCalculationResult discountResult = discountService.validateAndCalculate(
                    request.getDiscountCode(), subtotal, customer.getId()
            );
            if (discountResult.isValid()) {
                totalDiscount = discountResult.getCalculatedDiscountAmount();
            }
        }

        BigDecimal discountedSubtotal = subtotal.subtract(totalDiscount);
        if (discountedSubtotal.compareTo(BigDecimal.ZERO) < 0) discountedSubtotal = BigDecimal.ZERO;

        TaxCalculationResult taxResult = taxCalculationService.calculateTaxes(discountedSubtotal, customer.getState());
        BigDecimal totalTax = taxResult.getTotalTax();

        subscription.setSubtotal(subtotal);
        subscription.setDiscountAmount(totalDiscount);
        subscription.setTaxAmount(totalTax);
        subscription.setTotalAmount(discountedSubtotal.add(totalTax));

        Subscription saved = subscriptionRepository.save(subscription);

        // Record history
        SubscriptionHistory history = SubscriptionHistory.builder()
                .subscription(saved)
                .fromStatus(null)
                .toStatus(initialStatus)
                .changedBy(currentUserId)
                .reason("Initial subscription creation")
                .build();
        subscriptionHistoryRepository.save(history);

        log.info("Created subscription {} with status {}", saved.getSubscriptionNumber(), initialStatus);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse createFromQuotation(UUID quotationId, UUID currentUserId) {
        Quotation quotation = quotationRepository.findWithDetailsById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found: " + quotationId));

        if (!"CONFIRMED".equalsIgnoreCase(quotation.getStatus())) {
            throw new BusinessRuleException("Only CONFIRMED quotations can be converted to subscriptions. Status is: " + quotation.getStatus());
        }

        Customer customer = quotation.getCustomer();

        // Identify plan from quotation lines
        Plan plan = null;
        for (QuotationLine line : quotation.getLines()) {
            if (line.getPlanId() != null) {
                plan = planRepository.findById(line.getPlanId()).orElse(null);
                if (plan != null) break;
            }
        }

        if (plan == null) {
            // Pick default plan or first active plan
            List<Plan> activePlans = planRepository.findByIsActiveTrue();
            if (activePlans.isEmpty()) {
                throw new BusinessRuleException("No active plans available to attach to subscription");
            }
            plan = activePlans.get(0);
        }

        String subNumber = sequenceGeneratorService.generateSubscriptionNumber();
        LocalDate startDate = LocalDate.now();
        LocalDate nextBillingDate = calculateNextBillingDate(startDate, plan.getBillingInterval(), plan.getBillingIntervalCount());

        Subscription subscription = Subscription.builder()
                .subscriptionNumber(subNumber)
                .customer(customer)
                .plan(plan)
                .quotationId(quotation.getId())
                .status("ACTIVE")
                .billingInterval(plan.getBillingInterval())
                .billingIntervalCount(plan.getBillingIntervalCount())
                .startDate(startDate)
                .endDate(null)
                .nextBillingDate(nextBillingDate)
                .unitPrice(plan.getPrice())
                .quantity(1)
                .subtotal(quotation.getSubtotal())
                .discountAmount(quotation.getDiscountAmount())
                .taxAmount(quotation.getTaxAmount())
                .totalAmount(quotation.getTotalAmount())
                .paymentTerms("Immediate")
                .lines(new ArrayList<>())
                .history(new LinkedHashSet<>())
                .build();

        for (QuotationLine ql : quotation.getLines()) {
            SubscriptionLine line = SubscriptionLine.builder()
                    .subscription(subscription)
                    .productId(ql.getProductId())
                    .variantId(ql.getVariantId())
                    .description(ql.getDescription())
                    .quantity(ql.getQuantity())
                    .unitPrice(ql.getUnitPrice())
                    .taxRate(ql.getTaxRate())
                    .discountAmount(ql.getDiscountAmount())
                    .lineTotal(ql.getLineTotal())
                    .build();
            subscription.getLines().add(line);
        }

        Subscription saved = subscriptionRepository.save(subscription);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .subscription(saved)
                .fromStatus(quotation.getStatus())
                .toStatus("ACTIVE")
                .changedBy(currentUserId)
                .reason("Converted from Quotation " + quotation.getQuotationNumber())
                .build();
        subscriptionHistoryRepository.save(history);

        log.info("Converted quotation {} to subscription {}", quotation.getQuotationNumber(), saved.getSubscriptionNumber());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse changeStatus(UUID id, SubscriptionStatusChangeRequest request, UUID currentUserId) {
        Subscription subscription = subscriptionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        String currentStatus = subscription.getStatus();
        String targetStatus = request.getStatus().toUpperCase().trim();

        if (currentStatus.equalsIgnoreCase(targetStatus)) {
            return mapToResponse(subscription);
        }

        validateTransition(currentStatus, targetStatus);

        subscription.setStatus(targetStatus);
        if ("CLOSED".equalsIgnoreCase(targetStatus)) {
            subscription.setAutoCloseReason(request.getReason());
            if (subscription.getEndDate() == null) {
                subscription.setEndDate(LocalDate.now());
            }
        }

        Subscription saved = subscriptionRepository.save(subscription);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .subscription(saved)
                .fromStatus(currentStatus)
                .toStatus(targetStatus)
                .changedBy(currentUserId)
                .reason(request.getReason() != null ? request.getReason() : "Status change to " + targetStatus)
                .build();
        subscriptionHistoryRepository.save(history);

        log.info("Subscription {} transitioned from {} to {}", saved.getSubscriptionNumber(), currentStatus, targetStatus);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse changePlan(UUID id, SubscriptionPlanChangeRequest request, UUID currentUserId) {
        Subscription subscription = subscriptionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        Plan newPlan = planRepository.findById(request.getNewPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Target plan not found: " + request.getNewPlanId()));

        String oldPlanName = subscription.getPlan().getName();
        subscription.setPlan(newPlan);
        subscription.setBillingInterval(newPlan.getBillingInterval());
        subscription.setBillingIntervalCount(newPlan.getBillingIntervalCount());
        subscription.setUnitPrice(newPlan.getPrice());

        // Recalculate totals
        BigDecimal subtotal = newPlan.getPrice().multiply(BigDecimal.valueOf(subscription.getQuantity()));
        for (SubscriptionLine line : subscription.getLines()) {
            subtotal = subtotal.add(line.getLineTotal());
        }

        BigDecimal discounted = subtotal.subtract(subscription.getDiscountAmount());
        if (discounted.compareTo(BigDecimal.ZERO) < 0) discounted = BigDecimal.ZERO;

        TaxCalculationResult taxRes = taxCalculationService.calculateTaxes(discounted, subscription.getCustomer().getState());
        subscription.setSubtotal(subtotal);
        subscription.setTaxAmount(taxRes.getTotalTax());
        subscription.setTotalAmount(discounted.add(taxRes.getTotalTax()));

        Subscription saved = subscriptionRepository.save(subscription);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .subscription(saved)
                .fromStatus(subscription.getStatus())
                .toStatus(subscription.getStatus())
                .changedBy(currentUserId)
                .reason("Plan changed from " + oldPlanName + " to " + newPlan.getName() + ". " + (request.getReason() != null ? request.getReason() : ""))
                .build();
        subscriptionHistoryRepository.save(history);

        log.info("Subscription {} upgraded/downgraded to plan {}", saved.getSubscriptionNumber(), newPlan.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(UUID id, Integer additionalIntervals, UUID currentUserId) {
        Subscription subscription = subscriptionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        int intervals = additionalIntervals != null && additionalIntervals > 0 ? additionalIntervals : 1;
        LocalDate baseDate = LocalDate.now();
        if (subscription.getEndDate() != null && subscription.getEndDate().isAfter(baseDate)) {
            baseDate = subscription.getEndDate();
        }

        LocalDate newEndDate = calculateNextBillingDate(baseDate, subscription.getBillingInterval(), subscription.getBillingIntervalCount() * intervals);
        subscription.setEndDate(newEndDate);
        subscription.setStatus("ACTIVE");
        subscription.setNextBillingDate(LocalDate.now());

        Subscription saved = subscriptionRepository.save(subscription);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .subscription(saved)
                .fromStatus("RENEWED")
                .toStatus("ACTIVE")
                .changedBy(currentUserId)
                .reason("Renewed subscription for " + intervals + " interval(s) until " + newEndDate)
                .build();
        subscriptionHistoryRepository.save(history);

        log.info("Subscription {} renewed until {}", saved.getSubscriptionNumber(), newEndDate);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSubscription(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
        if ("ACTIVE".equalsIgnoreCase(subscription.getStatus())) {
            throw new BusinessRuleException("Cannot delete active subscription. Please close or pause it first.");
        }
        subscriptionRepository.delete(subscription);
        log.info("Deleted subscription id: {}", id);
    }

    private void validateTransition(String from, String to) {
        if ("CLOSED".equalsIgnoreCase(from) && !"ACTIVE".equalsIgnoreCase(to)) {
            throw new BusinessRuleException("Closed subscriptions can only be reactivated/renewed to ACTIVE.");
        }
        // Valid transitions check
        boolean valid = switch (from) {
            case "DRAFT" -> List.of("QUOTATION", "CONFIRMED", "ACTIVE", "CLOSED").contains(to);
            case "QUOTATION" -> List.of("CONFIRMED", "CLOSED").contains(to);
            case "CONFIRMED" -> List.of("ACTIVE", "CLOSED").contains(to);
            case "ACTIVE" -> List.of("PAUSED", "CLOSED", "EXPIRED").contains(to);
            case "PAUSED" -> List.of("ACTIVE", "CLOSED").contains(to);
            case "EXPIRED" -> List.of("ACTIVE", "CLOSED").contains(to);
            default -> false;
        };

        if (!valid) {
            throw new BusinessRuleException("Invalid status transition from " + from + " to " + to);
        }
    }

    private LocalDate calculateNextBillingDate(LocalDate from, String interval, int count) {
        if (count <= 0) count = 1;
        return switch (interval.toUpperCase()) {
            case "DAILY" -> from.plusDays(count);
            case "WEEKLY" -> from.plusWeeks(count);
            case "MONTHLY" -> from.plusMonths(count);
            case "QUARTERLY" -> from.plusMonths(3L * count);
            case "YEARLY" -> from.plusYears(count);
            default -> from.plusMonths(count);
        };
    }

    private SubscriptionResponse mapToResponse(Subscription s) {
        List<SubscriptionLineResponse> lines = s.getLines() != null ?
                s.getLines().stream().map(line -> {
                    String prodName = null;
                    if (line.getProductId() != null) {
                        prodName = productRepository.findById(line.getProductId())
                                .map(Product::getName).orElse(null);
                    }
                    return SubscriptionLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(prodName)
                            .variantId(line.getVariantId())
                            .description(line.getDescription())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .taxRate(line.getTaxRate())
                            .discountAmount(line.getDiscountAmount())
                            .lineTotal(line.getLineTotal())
                            .build();
                }).collect(Collectors.toList()) : new ArrayList<>();

        List<SubscriptionHistoryResponse> history = s.getHistory() != null ?
                s.getHistory().stream().map(h -> {
                    String changedByName = null;
                    if (h.getChangedBy() != null) {
                        changedByName = userRepository.findById(h.getChangedBy())
                                .map(User::getFullName).orElse(null);
                    }
                    return SubscriptionHistoryResponse.builder()
                            .id(h.getId())
                            .fromStatus(h.getFromStatus())
                            .toStatus(h.getToStatus())
                            .changedBy(h.getChangedBy())
                            .changedByName(changedByName)
                            .reason(h.getReason())
                            .createdAt(h.getCreatedAt())
                            .build();
                }).collect(Collectors.toList()) : new ArrayList<>();

        return SubscriptionResponse.builder()
                .id(s.getId())
                .subscriptionNumber(s.getSubscriptionNumber())
                .customerId(s.getCustomer() != null ? s.getCustomer().getId() : null)
                .customerName(s.getCustomer() != null ? s.getCustomer().getName() : null)
                .customerEmail(s.getCustomer() != null ? s.getCustomer().getEmail() : null)
                .planId(s.getPlan() != null ? s.getPlan().getId() : null)
                .planName(s.getPlan() != null ? s.getPlan().getName() : null)
                .planCode(s.getPlan() != null ? s.getPlan().getCode() : null)
                .quotationId(s.getQuotationId())
                .status(s.getStatus())
                .billingInterval(s.getBillingInterval())
                .billingIntervalCount(s.getBillingIntervalCount())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .nextBillingDate(s.getNextBillingDate())
                .lastBillingDate(s.getLastBillingDate())
                .unitPrice(s.getUnitPrice())
                .quantity(s.getQuantity())
                .subtotal(s.getSubtotal())
                .taxAmount(s.getTaxAmount())
                .discountAmount(s.getDiscountAmount())
                .totalAmount(s.getTotalAmount())
                .autoCloseReason(s.getAutoCloseReason())
                .paymentTerms(s.getPaymentTerms())
                .lines(lines)
                .history(history)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
