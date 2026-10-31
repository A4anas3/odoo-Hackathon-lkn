package com.brightloop.invoice.repo;

import com.brightloop.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @EntityGraph(attributePaths = {"customer", "subscription", "lines", "taxLines"})
    Optional<Invoice> findWithDetailsById(UUID id);

    @Query("SELECT i FROM Invoice i JOIN i.customer c WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:status AS string) IS NULL OR i.status = :status) AND " +
           "(:customerId IS NULL OR c.id = :customerId) AND " +
           "(:subscriptionId IS NULL OR i.subscription.id = :subscriptionId)")
    Page<Invoice> findWithFilters(@Param("search") String search,
                                 @Param("status") String status,
                                 @Param("customerId") UUID customerId,
                                 @Param("subscriptionId") UUID subscriptionId,
                                 Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.status NOT IN ('PAID', 'CANCELLED') AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i WHERE i.status != 'CANCELLED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.status NOT IN ('PAID', 'CANCELLED')")
    BigDecimal calculateTotalOutstanding();

    List<Invoice> findByCustomerIdAndStatusIn(UUID customerId, List<String> statuses);
}
