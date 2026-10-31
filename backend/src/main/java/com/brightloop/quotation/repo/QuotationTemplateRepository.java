package com.brightloop.quotation.repo;

import com.brightloop.quotation.entity.QuotationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuotationTemplateRepository extends JpaRepository<QuotationTemplate, UUID> {

    List<QuotationTemplate> findByIsActiveTrue();

    @Query("SELECT t FROM QuotationTemplate t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<QuotationTemplate> findWithFilters(@Param("search") String search,
                                           @Param("isActive") Boolean isActive,
                                           Pageable pageable);
}
