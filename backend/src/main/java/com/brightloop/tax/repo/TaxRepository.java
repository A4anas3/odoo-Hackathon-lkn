package com.brightloop.tax.repo;

import com.brightloop.tax.entity.Tax;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaxRepository extends JpaRepository<Tax, UUID> {

    List<Tax> findByIsActiveTrue();

    @Query("SELECT t FROM Tax t WHERE t.isActive = true AND " +
           "(t.validFrom IS NULL OR t.validFrom <= :date) AND " +
           "(t.validUntil IS NULL OR t.validUntil >= :date)")
    List<Tax> findActiveTaxesOnDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM Tax t WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(t.taxType) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(:isActive IS NULL OR t.isActive = :isActive)")
    Page<Tax> findWithFilters(@Param("search") String search,
                              @Param("isActive") Boolean isActive,
                              Pageable pageable);
}
