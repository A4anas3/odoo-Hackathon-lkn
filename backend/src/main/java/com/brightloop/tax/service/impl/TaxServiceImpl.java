package com.brightloop.tax.service.impl;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.tax.dto.TaxRequest;
import com.brightloop.tax.dto.TaxResponse;
import com.brightloop.tax.entity.Tax;
import com.brightloop.tax.repo.TaxRepository;
import com.brightloop.tax.service.TaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaxResponse> listTaxes(String search, Boolean isActive, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Tax> page = taxRepository.findWithFilters(cleanSearch, isActive, pageable);
        List<TaxResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<TaxResponse>builder()
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
    public List<TaxResponse> listActiveTaxes() {
        return taxRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaxResponse getTaxById(UUID id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + id));
        return mapToResponse(tax);
    }

    @Override
    @Transactional
    public TaxResponse createTax(TaxRequest request) {
        Tax tax = Tax.builder()
                .name(request.getName().trim())
                .taxType(request.getTaxType().toUpperCase().trim())
                .rate(request.getRate())
                .country(request.getCountry() != null ? request.getCountry().trim() : "India")
                .state(request.getState() != null ? request.getState().trim() : null)
                .isCompound(request.getIsCompound() != null ? request.getIsCompound() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();

        Tax saved = taxRepository.save(tax);
        log.info("Created tax {} with rate {}", saved.getName(), saved.getRate());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TaxResponse updateTax(UUID id, TaxRequest request) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + id));

        tax.setName(request.getName().trim());
        tax.setTaxType(request.getTaxType().toUpperCase().trim());
        tax.setRate(request.getRate());
        if (request.getCountry() != null) tax.setCountry(request.getCountry().trim());
        tax.setState(request.getState() != null ? request.getState().trim() : null);
        if (request.getIsCompound() != null) tax.setIsCompound(request.getIsCompound());
        if (request.getIsActive() != null) tax.setIsActive(request.getIsActive());
        tax.setValidFrom(request.getValidFrom());
        tax.setValidUntil(request.getValidUntil());

        Tax updated = taxRepository.save(tax);
        log.info("Updated tax id: {}", id);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTax(UUID id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + id));
        tax.setIsActive(false);
        taxRepository.save(tax);
        log.info("Soft-deleted (deactivated) tax id: {}", id);
    }

    private TaxResponse mapToResponse(Tax tax) {
        return TaxResponse.builder()
                .id(tax.getId())
                .name(tax.getName())
                .taxType(tax.getTaxType())
                .rate(tax.getRate())
                .country(tax.getCountry())
                .state(tax.getState())
                .isCompound(tax.getIsCompound())
                .isActive(tax.getIsActive())
                .validFrom(tax.getValidFrom())
                .validUntil(tax.getValidUntil())
                .createdAt(tax.getCreatedAt())
                .build();
    }
}
