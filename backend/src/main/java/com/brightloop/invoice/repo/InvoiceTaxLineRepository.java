package com.brightloop.invoice.repo;

import com.brightloop.invoice.entity.InvoiceTaxLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceTaxLineRepository extends JpaRepository<InvoiceTaxLine, UUID> {
    List<InvoiceTaxLine> findByInvoiceId(UUID invoiceId);
}
