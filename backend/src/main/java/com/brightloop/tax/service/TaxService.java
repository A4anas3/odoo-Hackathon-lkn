package com.brightloop.tax.service;

import com.brightloop.common.dto.PageResponse;
import com.brightloop.tax.dto.TaxRequest;
import com.brightloop.tax.dto.TaxResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaxService {
    PageResponse<TaxResponse> listTaxes(String search, Boolean isActive, Pageable pageable);
    List<TaxResponse> listActiveTaxes();
    TaxResponse getTaxById(UUID id);
    TaxResponse createTax(TaxRequest request);
    TaxResponse updateTax(UUID id, TaxRequest request);
    void deleteTax(UUID id);
}
