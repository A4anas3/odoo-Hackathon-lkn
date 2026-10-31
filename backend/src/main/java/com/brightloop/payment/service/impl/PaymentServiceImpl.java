package com.brightloop.payment.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.BadRequestException;
import com.brightloop.common.exception.BusinessRuleException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.common.service.SequenceGeneratorService;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repo.CustomerRepository;
import com.brightloop.invoice.entity.Invoice;
import com.brightloop.invoice.repo.InvoiceRepository;
import com.brightloop.invoice.service.InvoiceService;
import com.brightloop.payment.dto.PaymentAllocationItem;
import com.brightloop.payment.dto.PaymentAllocationResponse;
import com.brightloop.payment.dto.PaymentRequest;
import com.brightloop.payment.dto.PaymentResponse;
import com.brightloop.payment.entity.Payment;
import com.brightloop.payment.entity.PaymentAllocation;
import com.brightloop.payment.repo.PaymentAllocationRepository;
import com.brightloop.payment.repo.PaymentRepository;
import com.brightloop.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(
            String search, String method, String status, UUID customerId, Pageable pageable
    ) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanMethod = (method != null && !method.trim().isEmpty()) ? method.toUpperCase().trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.toUpperCase().trim() : null;

        Page<Payment> page = paymentRepository.findWithFilters(cleanSearch, cleanMethod, cleanStatus, customerId, pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PaymentResponse>builder()
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
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        String paymentNumber = sequenceGeneratorService.generatePaymentNumber();
        Instant paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : Instant.now();

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumber)
                .customer(customer)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod().toUpperCase().trim())
                .paymentDate(paymentDate)
                .status("COMPLETED")
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .allocations(new ArrayList<>())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            BigDecimal totalAllocated = BigDecimal.ZERO;
            for (PaymentAllocationItem item : request.getAllocations()) {
                totalAllocated = totalAllocated.add(item.getAmount());
            }

            if (totalAllocated.compareTo(request.getAmount()) > 0) {
                throw new BadRequestException("Total allocation (" + totalAllocated + ") cannot exceed payment amount (" + request.getAmount() + ")");
            }

            for (PaymentAllocationItem item : request.getAllocations()) {
                Invoice invoice = invoiceRepository.findById(item.getInvoiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + item.getInvoiceId()));

                if (!invoice.getCustomer().getId().equals(customer.getId())) {
                    throw new BadRequestException("Invoice " + invoice.getInvoiceNumber() + " does not belong to customer " + customer.getName());
                }

                if (item.getAmount().compareTo(invoice.getBalanceDue()) > 0) {
                    throw new BadRequestException("Allocated amount (" + item.getAmount() + ") exceeds invoice balance due (" + invoice.getBalanceDue() + ")");
                }

                PaymentAllocation allocation = PaymentAllocation.builder()
                        .payment(savedPayment)
                        .invoice(invoice)
                        .allocatedAmount(item.getAmount())
                        .allocatedAt(paymentDate)
                        .build();

                savedPayment.getAllocations().add(allocation);
                paymentAllocationRepository.save(allocation);

                // Update invoice
                invoiceService.recordPaymentAllocation(invoice.getId(), item.getAmount());
            }
        } else {
            // Auto-allocate across unpaid/partially paid invoices FIFO
            List<Invoice> openInvoices = invoiceRepository.findByCustomerIdAndStatusIn(
                    customer.getId(), List.of("POSTED", "PARTIALLY_PAID")
            );

            BigDecimal remaining = request.getAmount();
            for (Invoice invoice : openInvoices) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal toAllocate = remaining.min(invoice.getBalanceDue());
                if (toAllocate.compareTo(BigDecimal.ZERO) > 0) {
                    PaymentAllocation allocation = PaymentAllocation.builder()
                            .payment(savedPayment)
                            .invoice(invoice)
                            .allocatedAmount(toAllocate)
                            .allocatedAt(paymentDate)
                            .build();

                    savedPayment.getAllocations().add(allocation);
                    paymentAllocationRepository.save(allocation);

                    invoiceService.recordPaymentAllocation(invoice.getId(), toAllocate);
                    remaining = remaining.subtract(toAllocate);
                }
            }
        }

        log.info("Recorded payment {} of ₹{} for customer {}", savedPayment.getPaymentNumber(), savedPayment.getAmount(), customer.getName());
        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID id, String reason) {
        Payment payment = paymentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));

        if (!"COMPLETED".equalsIgnoreCase(payment.getStatus())) {
            throw new BusinessRuleException("Only COMPLETED payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus("REFUNDED");
        payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "") + "Refunded: " + (reason != null ? reason : ""));

        // Reverse invoice allocations if necessary
        for (PaymentAllocation allocation : payment.getAllocations()) {
            Invoice inv = allocation.getInvoice();
            BigDecimal newPaid = inv.getPaidAmount().subtract(allocation.getAllocatedAmount());
            if (newPaid.compareTo(BigDecimal.ZERO) < 0) newPaid = BigDecimal.ZERO;
            inv.setPaidAmount(newPaid);
            inv.setBalanceDue(inv.getTotalAmount().subtract(newPaid));
            inv.setStatus("POSTED");
            invoiceRepository.save(inv);
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Refunded payment {}", saved.getPaymentNumber());
        return mapToResponse(saved);
    }

    private PaymentResponse mapToResponse(Payment p) {
        List<PaymentAllocationResponse> allocations = p.getAllocations() != null ?
                p.getAllocations().stream().map(a -> PaymentAllocationResponse.builder()
                        .id(a.getId())
                        .invoiceId(a.getInvoice() != null ? a.getInvoice().getId() : null)
                        .invoiceNumber(a.getInvoice() != null ? a.getInvoice().getInvoiceNumber() : null)
                        .allocatedAmount(a.getAllocatedAmount())
                        .allocatedAt(a.getAllocatedAt())
                        .build()).collect(Collectors.toList()) : new ArrayList<>();

        return PaymentResponse.builder()
                .id(p.getId())
                .paymentNumber(p.getPaymentNumber())
                .customerId(p.getCustomer() != null ? p.getCustomer().getId() : null)
                .customerName(p.getCustomer() != null ? p.getCustomer().getName() : null)
                .customerEmail(p.getCustomer() != null ? p.getCustomer().getEmail() : null)
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .paymentDate(p.getPaymentDate())
                .status(p.getStatus())
                .referenceNumber(p.getReferenceNumber())
                .notes(p.getNotes())
                .allocations(allocations)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
