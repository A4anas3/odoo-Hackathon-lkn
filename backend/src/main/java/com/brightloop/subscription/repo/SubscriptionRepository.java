package com.brightloop.subscription.repo;

import com.brightloop.subscription.entity.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @EntityGraph(attributePaths = {"customer", "plan", "lines", "history"})
    Optional<Subscription> findWithDetailsById(UUID id);

    @Query("SELECT s FROM Subscription s JOIN s.customer c JOIN s.plan p WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(s.subscriptionNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:status AS string) IS NULL OR s.status = :status) AND " +
           "(:customerId IS NULL OR c.id = :customerId) AND " +
           "(:planId IS NULL OR p.id = :planId)")
    Page<Subscription> findWithFilters(@Param("search") String search,
                                      @Param("status") String status,
                                      @Param("customerId") UUID customerId,
                                      @Param("planId") UUID planId,
                                      Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.nextBillingDate <= :date")
    List<Subscription> findDueSubscriptions(@Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.id = :customerId AND s.status = 'ACTIVE'")
    long countActiveByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COALESCE(SUM(" +
           "CASE " +
           "  WHEN s.billingInterval = 'DAILY' THEN s.totalAmount * 30.0 " +
           "  WHEN s.billingInterval = 'WEEKLY' THEN s.totalAmount * 4.33 " +
           "  WHEN s.billingInterval = 'MONTHLY' THEN s.totalAmount / s.billingIntervalCount " +
           "  WHEN s.billingInterval = 'QUARTERLY' THEN s.totalAmount / 3.0 " +
           "  WHEN s.billingInterval = 'YEARLY' THEN s.totalAmount / 12.0 " +
           "  ELSE s.totalAmount " +
           "END), 0) FROM Subscription s WHERE s.status = 'ACTIVE'")
    BigDecimal calculateMrr();
}
