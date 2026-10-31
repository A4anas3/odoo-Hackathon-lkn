package com.brightloop.billing.repo;

import com.brightloop.billing.entity.BillingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BillingRecordRepository extends JpaRepository<BillingRecord, UUID> {

    @EntityGraph(attributePaths = {"subscription", "invoice"})
    @Query("SELECT b FROM BillingRecord b WHERE " +
           "(:subscriptionId IS NULL OR b.subscription.id = :subscriptionId) AND " +
           "(CAST(:status AS string) IS NULL OR b.status = :status)")
    Page<BillingRecord> findWithFilters(@Param("subscriptionId") UUID subscriptionId,
                                       @Param("status") String status,
                                       Pageable pageable);

    List<BillingRecord> findBySubscriptionIdOrderByBillingDateDesc(UUID subscriptionId);
}
