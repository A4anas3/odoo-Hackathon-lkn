package com.brightloop.invoice.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.BusinessRuleException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.common.service.SequenceGeneratorService;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repo.CustomerRepository;
import com.brightloop.discount.dto.DiscountCalculationResult;
import com.brightloop.discount.service.DiscountService;
import com.brightloop.invoice.dto.InvoiceLineRequest;
import com.brightloop.invoice.dto.InvoiceLineResponse;
import com.brightloop.invoice.dto.InvoiceRequest;
import com.brightloop.invoice.dto.InvoiceResponse;
import com.brightloop.invoice.dto.InvoiceTaxLineResponse;
import com.brightloop.invoice.entity.Invoice;
import com.brightloop.invoice.entity.InvoiceLine;
import com.brightloop.invoice.entity.InvoiceTaxLine;
import com.brightloop.invoice.repo.InvoiceRepository;
import com.brightloop.invoice.service.InvoiceService;
import com.brightloop.product.entity.Product;
import com.brightloop.product.repo.ProductRepository;
import com.brightloop.subscription.entity.Subscription;
import com.brightloop.subscription.entity.SubscriptionLine;
import com.brightloop.subscription.repo.SubscriptionRepository;
import com.brightloop.tax.dto.TaxCalculationResult;
import com.brightloop.tax.service.TaxCalculationService;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final TaxCalculationService taxCalculationService;
    private final DiscountService discountService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> listInvoices(
            String search, String status, UUID customerId, UUID subscriptionId, Pageable pageable
    ) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.toUpperCase().trim() : null;

        Page<Invoice> page = invoiceRepository.findWithFilters(cleanSearch, cleanStatus, customerId, subscriptionId, pageable);
        List<InvoiceResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<InvoiceResponse>builder()
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
    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return mapToResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        Subscription subscription = null;
        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId()).orElse(null);
        }

        String invoiceNumber = sequenceGeneratorService.generateInvoiceNumber();
        LocalDate issueDate = request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now();
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : issueDate.plusDays(15);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .subscription(subscription)
                .status("DRAFT")
                .issueDate(issueDate)
                .dueDate(dueDate)
                .currency("INR")
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : "Net 15")
                .notes(request.getNotes())
                .billingPeriodStart(request.getBillingPeriodStart())
                .billingPeriodEnd(request.getBillingPeriodEnd())
                .lines(new ArrayList<>())
                .taxLines(new LinkedHashSet<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceLineRequest lr : request.getLines()) {
            BigDecimal qty = BigDecimal.valueOf(lr.getQuantity());
            BigDecimal linePrice = lr.getUnitPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = lr.getDiscountAmount() != null ? lr.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineTotal = linePrice.subtract(discount);
            if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

            InvoiceLine line = InvoiceLine.builder()
                    .invoice(invoice)
                    .productId(lr.getProductId())
                    .variantId(lr.getVariantId())
                    .description(lr.getDescription())
                    .quantity(lr.getQuantity())
                    .unitPrice(lr.getUnitPrice())
                    .taxRate(lr.getTaxRate() != null ? lr.getTaxRate() : BigDecimal.ZERO)
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .build();

            invoice.getLines().add(line);
            subtotal = subtotal.add(lineTotal);
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

        for (TaxCalculationResult.TaxDetail td : taxResult.getBreakdown()) {
            InvoiceTaxLine taxLine = InvoiceTaxLine.builder()
                    .invoice(invoice)
                    .taxId(td.getTaxId())
                    .taxName(td.getName())
                    .taxRate(td.getRate())
                    .taxableAmount(discountedSubtotal)
                    .taxAmount(td.getTaxAmount())
                    .build();
            invoice.getTaxLines().add(taxLine);
        }

        BigDecimal totalAmount = discountedSubtotal.add(totalTax);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setTaxAmount(totalTax);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceDue(totalAmount);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice {} for customer {}", saved.getInvoiceNumber(), customer.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(UUID id, InvoiceRequest request) {
        Invoice invoice = invoiceRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if (!"DRAFT".equalsIgnoreCase(invoice.getStatus())) {
            throw new BusinessRuleException("Only DRAFT invoices can be edited. Current status: " + invoice.getStatus());
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        invoice.setCustomer(customer);
        if (request.getIssueDate() != null) invoice.setIssueDate(request.getIssueDate());
        if (request.getDueDate() != null) invoice.setDueDate(request.getDueDate());
        if (request.getPaymentTerms() != null) invoice.setPaymentTerms(request.getPaymentTerms());
        invoice.setNotes(request.getNotes());
        invoice.setBillingPeriodStart(request.getBillingPeriodStart());
        invoice.setBillingPeriodEnd(request.getBillingPeriodEnd());

        invoice.getLines().clear();
        invoice.getTaxLines().clear();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceLineRequest lr : request.getLines()) {
            BigDecimal qty = BigDecimal.valueOf(lr.getQuantity());
            BigDecimal linePrice = lr.getUnitPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = lr.getDiscountAmount() != null ? lr.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineTotal = linePrice.subtract(discount);
            if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

            InvoiceLine line = InvoiceLine.builder()
                    .invoice(invoice)
                    .productId(lr.getProductId())
                    .variantId(lr.getVariantId())
                    .description(lr.getDescription())
                    .quantity(lr.getQuantity())
                    .unitPrice(lr.getUnitPrice())
                    .taxRate(lr.getTaxRate() != null ? lr.getTaxRate() : BigDecimal.ZERO)
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .build();

            invoice.getLines().add(line);
            subtotal = subtotal.add(lineTotal);
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

        for (TaxCalculationResult.TaxDetail td : taxResult.getBreakdown()) {
            InvoiceTaxLine taxLine = InvoiceTaxLine.builder()
                    .invoice(invoice)
                    .taxId(td.getTaxId())
                    .taxName(td.getName())
                    .taxRate(td.getRate())
                    .taxableAmount(discountedSubtotal)
                    .taxAmount(td.getTaxAmount())
                    .build();
            invoice.getTaxLines().add(taxLine);
        }

        BigDecimal totalAmount = discountedSubtotal.add(totalTax);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setTaxAmount(totalTax);
        invoice.setTotalAmount(totalAmount);
        invoice.setBalanceDue(totalAmount.subtract(invoice.getPaidAmount()));

        Invoice updated = invoiceRepository.save(invoice);
        log.info("Updated invoice {}", updated.getInvoiceNumber());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public InvoiceResponse postInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if (!"DRAFT".equalsIgnoreCase(invoice.getStatus())) {
            throw new BusinessRuleException("Only DRAFT invoices can be posted. Current status: " + invoice.getStatus());
        }

        invoice.setStatus("POSTED");
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Posted invoice {}", saved.getInvoiceNumber());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new BusinessRuleException("Cannot cancel a fully paid invoice. Process a refund first.");
        }

        invoice.setStatus("CANCELLED");
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Cancelled invoice {}", saved.getInvoiceNumber());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public Invoice createRecurringInvoice(Subscription subscription, LocalDate periodStart, LocalDate periodEnd) {
        Customer customer = subscription.getCustomer();
        String invoiceNumber = sequenceGeneratorService.generateInvoiceNumber();
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(15);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .subscription(subscription)
                .status("POSTED") // Automatically posted for recurring billing
                .issueDate(issueDate)
                .dueDate(dueDate)
                .currency("INR")
                .paymentTerms(subscription.getPaymentTerms())
                .notes("Recurring billing for plan: " + subscription.getPlan().getName() + " (" + periodStart + " to " + periodEnd + ")")
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .lines(new ArrayList<>())
                .taxLines(new LinkedHashSet<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // Add main subscription plan line
        BigDecimal planPrice = subscription.getUnitPrice().multiply(BigDecimal.valueOf(subscription.getQuantity()));
        InvoiceLine planLine = InvoiceLine.builder()
                .invoice(invoice)
                .description(subscription.getPlan().getName() + " - " + subscription.getBillingInterval() + " Subscription")
                .quantity(subscription.getQuantity())
                .unitPrice(subscription.getUnitPrice())
                .taxRate(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .lineTotal(planPrice)
                .build();
        invoice.getLines().add(planLine);
        subtotal = subtotal.add(planPrice);

        // Add additional subscription lines if any
        if (subscription.getLines() != null) {
            for (SubscriptionLine sl : subscription.getLines()) {
                InvoiceLine line = InvoiceLine.builder()
                        .invoice(invoice)
                        .productId(sl.getProductId())
                        .variantId(sl.getVariantId())
                        .description(sl.getDescription() != null ? sl.getDescription() : "Subscription Item")
                        .quantity(sl.getQuantity())
                        .unitPrice(sl.getUnitPrice())
                        .taxRate(sl.getTaxRate())
                        .discountAmount(sl.getDiscountAmount())
                        .lineTotal(sl.getLineTotal())
                        .build();
                invoice.getLines().add(line);
                subtotal = subtotal.add(sl.getLineTotal());
            }
        }

        BigDecimal discountAmount = subscription.getDiscountAmount() != null ? subscription.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        if (discountedSubtotal.compareTo(BigDecimal.ZERO) < 0) discountedSubtotal = BigDecimal.ZERO;

        TaxCalculationResult taxResult = taxCalculationService.calculateTaxes(discountedSubtotal, customer.getState());
        BigDecimal totalTax = taxResult.getTotalTax();

        for (TaxCalculationResult.TaxDetail td : taxResult.getBreakdown()) {
            InvoiceTaxLine taxLine = InvoiceTaxLine.builder()
                    .invoice(invoice)
                    .taxId(td.getTaxId())
                    .taxName(td.getName())
                    .taxRate(td.getRate())
                    .taxableAmount(discountedSubtotal)
                    .taxAmount(td.getTaxAmount())
                    .build();
            invoice.getTaxLines().add(taxLine);
        }

        BigDecimal totalAmount = discountedSubtotal.add(totalTax);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTaxAmount(totalTax);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceDue(totalAmount);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created recurring invoice {} for subscription {}", saved.getInvoiceNumber(), subscription.getSubscriptionNumber());
        return saved;
    }

    @Override
    @Transactional
    public void recordPaymentAllocation(UUID invoiceId, BigDecimal allocatedAmount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(allocatedAmount);
        BigDecimal newBalanceDue = invoice.getTotalAmount().subtract(newPaidAmount);
        if (newBalanceDue.compareTo(BigDecimal.ZERO) < 0) newBalanceDue = BigDecimal.ZERO;

        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalanceDue(newBalanceDue);

        if (newBalanceDue.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus("PAID");
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepository.save(invoice);
        log.info("Recorded payment allocation of ₹{} on invoice {}. New balance: ₹{}, status: {}",
                allocatedAmount, invoice.getInvoiceNumber(), newBalanceDue, invoice.getStatus());
    }

    @Override
    @Transactional
    public void deleteInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        if (!"DRAFT".equalsIgnoreCase(invoice.getStatus())) {
            throw new BusinessRuleException("Only DRAFT invoices can be deleted. Cancel instead.");
        }

        invoiceRepository.delete(invoice);
        log.info("Deleted DRAFT invoice id: {}", id);
    }

    private InvoiceResponse mapToResponse(Invoice i) {
        List<InvoiceLineResponse> lines = i.getLines() != null ?
                i.getLines().stream().map(line -> {
                    String prodName = null;
                    if (line.getProductId() != null) {
                        prodName = productRepository.findById(line.getProductId())
                                .map(Product::getName).orElse(null);
                    }
                    return InvoiceLineResponse.builder()
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

        List<InvoiceTaxLineResponse> taxLines = i.getTaxLines() != null ?
                i.getTaxLines().stream().map(tl -> InvoiceTaxLineResponse.builder()
                        .id(tl.getId())
                        .taxId(tl.getTaxId())
                        .taxName(tl.getTaxName())
                        .taxRate(tl.getTaxRate())
                        .taxableAmount(tl.getTaxableAmount())
                        .taxAmount(tl.getTaxAmount())
                        .build()).collect(Collectors.toList()) : new ArrayList<>();

        return InvoiceResponse.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .customerId(i.getCustomer() != null ? i.getCustomer().getId() : null)
                .customerName(i.getCustomer() != null ? i.getCustomer().getName() : null)
                .customerEmail(i.getCustomer() != null ? i.getCustomer().getEmail() : null)
                .customerGstin(i.getCustomer() != null ? i.getCustomer().getGstin() : null)
                .subscriptionId(i.getSubscription() != null ? i.getSubscription().getId() : null)
                .subscriptionNumber(i.getSubscription() != null ? i.getSubscription().getSubscriptionNumber() : null)
                .status(i.getStatus())
                .issueDate(i.getIssueDate())
                .dueDate(i.getDueDate())
                .subtotal(i.getSubtotal())
                .taxAmount(i.getTaxAmount())
                .discountAmount(i.getDiscountAmount())
                .totalAmount(i.getTotalAmount())
                .paidAmount(i.getPaidAmount())
                .balanceDue(i.getBalanceDue())
                .currency(i.getCurrency())
                .notes(i.getNotes())
                .paymentTerms(i.getPaymentTerms())
                .billingPeriodStart(i.getBillingPeriodStart())
                .billingPeriodEnd(i.getBillingPeriodEnd())
                .pdfPath(i.getPdfPath())
                .lines(lines)
                .taxLines(taxLines)
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
