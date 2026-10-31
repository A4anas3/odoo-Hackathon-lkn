package com.brightloop.quotation.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.quotation.dto.QuotationRequest;
import com.brightloop.quotation.dto.QuotationResponse;
import com.brightloop.quotation.dto.QuotationTemplateRequest;
import com.brightloop.quotation.dto.QuotationTemplateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface QuotationService {
    PageResponse<QuotationResponse> listQuotations(String search, String status, UUID customerId, Pageable pageable);
    QuotationResponse getQuotationById(UUID id);
    QuotationResponse createQuotation(QuotationRequest request, UUID currentUserId);
    QuotationResponse updateQuotation(UUID id, QuotationRequest request);
    QuotationResponse updateStatus(UUID id, String status);
    void deleteQuotation(UUID id);

    // Template operations
    PageResponse<QuotationTemplateResponse> listTemplates(String search, Boolean isActive, Pageable pageable);
    List<QuotationTemplateResponse> listActiveTemplates();
    QuotationTemplateResponse getTemplateById(UUID id);
    QuotationTemplateResponse createTemplate(QuotationTemplateRequest request);
    QuotationTemplateResponse updateTemplate(UUID id, QuotationTemplateRequest request);
    void deleteTemplate(UUID id);
}
