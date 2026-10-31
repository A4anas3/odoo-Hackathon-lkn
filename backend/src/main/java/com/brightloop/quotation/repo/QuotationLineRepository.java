package com.brightloop.quotation.repo;

import com.brightloop.quotation.entity.QuotationLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuotationLineRepository extends JpaRepository<QuotationLine, UUID> {
    List<QuotationLine> findByQuotationId(UUID quotationId);
}
