package com.brightloop.invoice.service;

import com.brightloop.invoice.entity.Invoice;

import java.util.UUID;

public interface InvoicePdfService {
    byte[] generateInvoicePdf(UUID invoiceId);
    byte[] generateInvoicePdf(Invoice invoice);
}
