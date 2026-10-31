package com.brightloop.payment.repo;

import com.brightloop.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(attributePaths = {"customer", "allocations", "allocations.invoice"})
    Optional<Payment> findWithDetailsById(UUID id);

    @Query("SELECT p FROM Payment p JOIN p.customer c WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(p.paymentNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(p.referenceNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:method AS string) IS NULL OR p.paymentMethod = :method) AND " +
           "(CAST(:status AS string) IS NULL OR p.status = :status) AND " +
           "(:customerId IS NULL OR c.id = :customerId)")
    Page<Payment> findWithFilters(@Param("search") String search,
                                 @Param("method") String method,
                                 @Param("status") String status,
                                 @Param("customerId") UUID customerId,
                                 Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal calculateTotalCollected();
}
