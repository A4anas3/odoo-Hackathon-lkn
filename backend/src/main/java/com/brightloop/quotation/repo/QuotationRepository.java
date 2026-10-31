package com.brightloop.quotation.repo;

import com.brightloop.quotation.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    @EntityGraph(attributePaths = {"customer", "template", "lines"})
    Optional<Quotation> findWithDetailsById(UUID id);

    @Query("SELECT q FROM Quotation q JOIN q.customer c WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:status AS string) IS NULL OR q.status = :status) AND " +
           "(:customerId IS NULL OR c.id = :customerId)")
    Page<Quotation> findWithFilters(@Param("search") String search,
                                   @Param("status") String status,
                                   @Param("customerId") UUID customerId,
                                   Pageable pageable);

    long countByStatus(String status);
}
