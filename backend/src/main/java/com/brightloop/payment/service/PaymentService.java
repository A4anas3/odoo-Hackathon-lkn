package com.brightloop.payment.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.payment.dto.PaymentRequest;
import com.brightloop.payment.dto.PaymentResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {
    PageResponse<PaymentResponse> listPayments(String search, String method, String status, UUID customerId, Pageable pageable);
    PaymentResponse getPaymentById(UUID id);
    PaymentResponse recordPayment(PaymentRequest request);
    PaymentResponse refundPayment(UUID id, String reason);
}
