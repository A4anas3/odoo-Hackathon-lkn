package com.brightloop.payment.repo;

import com.brightloop.payment.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {
    List<PaymentAllocation> findByPaymentId(UUID paymentId);
    List<PaymentAllocation> findByInvoiceId(UUID invoiceId);
}
