package com.brightloop.invoice.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.invoice.dto.InvoiceRequest;
import com.brightloop.invoice.dto.InvoiceResponse;
import com.brightloop.invoice.entity.Invoice;
import com.brightloop.subscription.entity.Subscription;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InvoiceService {
    PageResponse<InvoiceResponse> listInvoices(String search, String status, UUID customerId, UUID subscriptionId, Pageable pageable);
    InvoiceResponse getInvoiceById(UUID id);
    InvoiceResponse createInvoice(InvoiceRequest request);
    InvoiceResponse updateInvoice(UUID id, InvoiceRequest request);
    InvoiceResponse postInvoice(UUID id);
    InvoiceResponse cancelInvoice(UUID id);
    Invoice createRecurringInvoice(Subscription subscription, LocalDate periodStart, LocalDate periodEnd);
    void recordPaymentAllocation(UUID invoiceId, BigDecimal allocatedAmount);
    void deleteInvoice(UUID id);
}
