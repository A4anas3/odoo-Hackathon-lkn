package com.brightloop.quotation.service.impl;

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
import com.brightloop.product.entity.ProductVariant;
import com.brightloop.product.repo.ProductRepository;
import com.brightloop.product.repo.ProductVariantRepository;
import com.brightloop.quotation.dto.QuotationLineRequest;
import com.brightloop.quotation.dto.QuotationLineResponse;
import com.brightloop.quotation.dto.QuotationRequest;
import com.brightloop.quotation.dto.QuotationResponse;
import com.brightloop.quotation.dto.QuotationTemplateLineDto;
import com.brightloop.quotation.dto.QuotationTemplateRequest;
import com.brightloop.quotation.dto.QuotationTemplateResponse;
import com.brightloop.quotation.entity.Quotation;
import com.brightloop.quotation.entity.QuotationLine;
import com.brightloop.quotation.entity.QuotationTemplate;
import com.brightloop.quotation.entity.QuotationTemplateLine;
import com.brightloop.quotation.repo.QuotationRepository;
import com.brightloop.quotation.repo.QuotationTemplateRepository;
import com.brightloop.quotation.service.QuotationService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final QuotationTemplateRepository quotationTemplateRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PlanRepository planRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final TaxCalculationService taxCalculationService;
    private final DiscountService discountService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuotationResponse> listQuotations(String search, String status, UUID customerId, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.toUpperCase().trim() : null;

        Page<Quotation> page = quotationRepository.findWithFilters(cleanSearch, cleanStatus, customerId, pageable);
        List<QuotationResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<QuotationResponse>builder()
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
    public QuotationResponse getQuotationById(UUID id) {
        Quotation quotation = quotationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));
        return mapToResponse(quotation);
    }

    @Override
    @Transactional
    public QuotationResponse createQuotation(QuotationRequest request, UUID currentUserId) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        QuotationTemplate template = null;
        if (request.getTemplateId() != null) {
            template = quotationTemplateRepository.findById(request.getTemplateId()).orElse(null);
        }

        String quotationNumber = sequenceGeneratorService.generateQuotationNumber();
        LocalDate orderDate = request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now();
        LocalDate validUntil = request.getValidUntil();
        if (validUntil == null) {
            int validityDays = (template != null) ? template.getValidityDays() : 30;
            validUntil = orderDate.plusDays(validityDays);
        }

        Quotation quotation = Quotation.builder()
                .quotationNumber(quotationNumber)
                .customer(customer)
                .template(template)
                .status("DRAFT")
                .orderDate(orderDate)
                .validUntil(validUntil)
                .termsAndConditions(request.getTermsAndConditions())
                .notes(request.getNotes())
                .createdBy(currentUserId)
                .lines(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (QuotationLineRequest lineReq : request.getLines()) {
            BigDecimal qty = BigDecimal.valueOf(lineReq.getQuantity());
            BigDecimal linePrice = lineReq.getUnitPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = lineReq.getDiscountAmount() != null ? lineReq.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineTotal = linePrice.subtract(discount);
            if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

            QuotationLine line = QuotationLine.builder()
                    .quotation(quotation)
                    .productId(lineReq.getProductId())
                    .variantId(lineReq.getVariantId())
                    .planId(lineReq.getPlanId())
                    .description(lineReq.getDescription())
                    .quantity(lineReq.getQuantity())
                    .unitPrice(lineReq.getUnitPrice())
                    .taxRate(lineReq.getTaxRate() != null ? lineReq.getTaxRate() : BigDecimal.ZERO)
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .build();

            quotation.getLines().add(line);
            subtotal = subtotal.add(lineTotal);
        }

        // Apply coupon discount if provided
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

        // Calculate tax based on customer state
        TaxCalculationResult taxResult = taxCalculationService.calculateTaxes(discountedSubtotal, customer.getState());
        BigDecimal totalTax = taxResult.getTotalTax();

        quotation.setSubtotal(subtotal);
        quotation.setDiscountAmount(totalDiscount);
        quotation.setTaxAmount(totalTax);
        quotation.setTotalAmount(discountedSubtotal.add(totalTax));

        Quotation saved = quotationRepository.save(quotation);
        log.info("Created quotation {} for customer {}", saved.getQuotationNumber(), customer.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotation(UUID id, QuotationRequest request) {
        Quotation quotation = quotationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        if (!"DRAFT".equalsIgnoreCase(quotation.getStatus())) {
            throw new BusinessRuleException("Only DRAFT quotations can be edited. Current status: " + quotation.getStatus());
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        quotation.setCustomer(customer);
        if (request.getOrderDate() != null) quotation.setOrderDate(request.getOrderDate());
        if (request.getValidUntil() != null) quotation.setValidUntil(request.getValidUntil());
        quotation.setTermsAndConditions(request.getTermsAndConditions());
        quotation.setNotes(request.getNotes());

        quotation.getLines().clear();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (QuotationLineRequest lineReq : request.getLines()) {
            BigDecimal qty = BigDecimal.valueOf(lineReq.getQuantity());
            BigDecimal linePrice = lineReq.getUnitPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = lineReq.getDiscountAmount() != null ? lineReq.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineTotal = linePrice.subtract(discount);
            if (lineTotal.compareTo(BigDecimal.ZERO) < 0) lineTotal = BigDecimal.ZERO;

            QuotationLine line = QuotationLine.builder()
                    .quotation(quotation)
                    .productId(lineReq.getProductId())
                    .variantId(lineReq.getVariantId())
                    .planId(lineReq.getPlanId())
                    .description(lineReq.getDescription())
                    .quantity(lineReq.getQuantity())
                    .unitPrice(lineReq.getUnitPrice())
                    .taxRate(lineReq.getTaxRate() != null ? lineReq.getTaxRate() : BigDecimal.ZERO)
                    .discountAmount(discount)
                    .lineTotal(lineTotal)
                    .build();

            quotation.getLines().add(line);
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

        quotation.setSubtotal(subtotal);
        quotation.setDiscountAmount(totalDiscount);
        quotation.setTaxAmount(totalTax);
        quotation.setTotalAmount(discountedSubtotal.add(totalTax));

        Quotation updated = quotationRepository.save(quotation);
        log.info("Updated quotation {}", updated.getQuotationNumber());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public QuotationResponse updateStatus(UUID id, String status) {
        Quotation quotation = quotationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        String targetStatus = status.toUpperCase().trim();
        // Allowed transitions: DRAFT -> SENT -> CONFIRMED, or DRAFT/SENT -> CANCELLED
        String currentStatus = quotation.getStatus();

        if (currentStatus.equalsIgnoreCase(targetStatus)) {
            return mapToResponse(quotation);
        }

        if ("CANCELLED".equals(targetStatus)) {
            quotation.setStatus("CANCELLED");
        } else if ("SENT".equals(targetStatus) && "DRAFT".equals(currentStatus)) {
            quotation.setStatus("SENT");
        } else if ("CONFIRMED".equals(targetStatus) && ("DRAFT".equals(currentStatus) || "SENT".equals(currentStatus))) {
            quotation.setStatus("CONFIRMED");
        } else {
            throw new BusinessRuleException("Invalid status transition from " + currentStatus + " to " + targetStatus);
        }

        Quotation saved = quotationRepository.save(quotation);
        log.info("Quotation {} transitioned to {}", saved.getQuotationNumber(), targetStatus);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteQuotation(UUID id) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));
        if ("CONFIRMED".equalsIgnoreCase(quotation.getStatus())) {
            throw new BusinessRuleException("Cannot delete confirmed quotation: " + quotation.getQuotationNumber());
        }
        quotationRepository.delete(quotation);
        log.info("Deleted quotation id: {}", id);
    }

    // Template operations
    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuotationTemplateResponse> listTemplates(String search, Boolean isActive, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<QuotationTemplate> page = quotationTemplateRepository.findWithFilters(cleanSearch, isActive, pageable);
        List<QuotationTemplateResponse> content = page.getContent().stream()
                .map(this::mapTemplateToResponse)
                .collect(Collectors.toList());

        return PageResponse.<QuotationTemplateResponse>builder()
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
    public List<QuotationTemplateResponse> listActiveTemplates() {
        return quotationTemplateRepository.findByIsActiveTrue().stream()
                .map(this::mapTemplateToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationTemplateResponse getTemplateById(UUID id) {
        QuotationTemplate template = quotationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        return mapTemplateToResponse(template);
    }

    @Override
    @Transactional
    public QuotationTemplateResponse createTemplate(QuotationTemplateRequest request) {
        QuotationTemplate template = QuotationTemplate.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .validityDays(request.getValidityDays() != null ? request.getValidityDays() : 30)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .lines(new ArrayList<>())
                .build();

        if (request.getLines() != null) {
            for (QuotationTemplateLineDto lineDto : request.getLines()) {
                QuotationTemplateLine line = QuotationTemplateLine.builder()
                        .template(template)
                        .productId(lineDto.getProductId())
                        .planId(lineDto.getPlanId())
                        .quantity(lineDto.getQuantity() != null ? lineDto.getQuantity() : 1)
                        .unitPrice(lineDto.getUnitPrice())
                        .build();
                template.getLines().add(line);
            }
        }

        QuotationTemplate saved = quotationTemplateRepository.save(template);
        log.info("Created quotation template: {}", saved.getName());
        return mapTemplateToResponse(saved);
    }

    @Override
    @Transactional
    public QuotationTemplateResponse updateTemplate(UUID id, QuotationTemplateRequest request) {
        QuotationTemplate template = quotationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));

        template.setName(request.getName().trim());
        template.setDescription(request.getDescription());
        if (request.getValidityDays() != null) template.setValidityDays(request.getValidityDays());
        if (request.getIsActive() != null) template.setIsActive(request.getIsActive());

        template.getLines().clear();
        if (request.getLines() != null) {
            for (QuotationTemplateLineDto lineDto : request.getLines()) {
                QuotationTemplateLine line = QuotationTemplateLine.builder()
                        .template(template)
                        .productId(lineDto.getProductId())
                        .planId(lineDto.getPlanId())
                        .quantity(lineDto.getQuantity() != null ? lineDto.getQuantity() : 1)
                        .unitPrice(lineDto.getUnitPrice())
                        .build();
                template.getLines().add(line);
            }
        }

        QuotationTemplate updated = quotationTemplateRepository.save(template);
        log.info("Updated quotation template: {}", updated.getName());
        return mapTemplateToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID id) {
        QuotationTemplate template = quotationTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        template.setIsActive(false);
        quotationTemplateRepository.save(template);
        log.info("Deactivated quotation template id: {}", id);
    }

    private QuotationResponse mapToResponse(Quotation q) {
        List<QuotationLineResponse> lines = q.getLines() != null ?
                q.getLines().stream().map(line -> {
                    String prodName = null;
                    if (line.getProductId() != null) {
                        prodName = productRepository.findById(line.getProductId())
                                .map(Product::getName).orElse(null);
                    }
                    String planName = null;
                    if (line.getPlanId() != null) {
                        planName = planRepository.findById(line.getPlanId())
                                .map(Plan::getName).orElse(null);
                    }
                    return QuotationLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(prodName)
                            .variantId(line.getVariantId())
                            .planId(line.getPlanId())
                            .planName(planName)
                            .description(line.getDescription())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .taxRate(line.getTaxRate())
                            .discountAmount(line.getDiscountAmount())
                            .lineTotal(line.getLineTotal())
                            .build();
                }).collect(Collectors.toList()) : new ArrayList<>();

        return QuotationResponse.builder()
                .id(q.getId())
                .quotationNumber(q.getQuotationNumber())
                .customerId(q.getCustomer() != null ? q.getCustomer().getId() : null)
                .customerName(q.getCustomer() != null ? q.getCustomer().getName() : null)
                .customerEmail(q.getCustomer() != null ? q.getCustomer().getEmail() : null)
                .templateId(q.getTemplate() != null ? q.getTemplate().getId() : null)
                .templateName(q.getTemplate() != null ? q.getTemplate().getName() : null)
                .status(q.getStatus())
                .orderDate(q.getOrderDate())
                .validUntil(q.getValidUntil())
                .subtotal(q.getSubtotal())
                .taxAmount(q.getTaxAmount())
                .discountAmount(q.getDiscountAmount())
                .totalAmount(q.getTotalAmount())
                .termsAndConditions(q.getTermsAndConditions())
                .notes(q.getNotes())
                .lines(lines)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }

    private QuotationTemplateResponse mapTemplateToResponse(QuotationTemplate t) {
        List<QuotationTemplateLineDto> lines = t.getLines() != null ?
                t.getLines().stream().map(l -> QuotationTemplateLineDto.builder()
                        .id(l.getId())
                        .productId(l.getProductId())
                        .productName(l.getProductId() != null ? productRepository.findById(l.getProductId()).map(Product::getName).orElse(null) : null)
                        .planId(l.getPlanId())
                        .planName(l.getPlanId() != null ? planRepository.findById(l.getPlanId()).map(Plan::getName).orElse(null) : null)
                        .quantity(l.getQuantity())
                        .unitPrice(l.getUnitPrice())
                        .build()).collect(Collectors.toList()) : new ArrayList<>();

        return QuotationTemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .validityDays(t.getValidityDays())
                .isActive(t.getIsActive())
                .lines(lines)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
