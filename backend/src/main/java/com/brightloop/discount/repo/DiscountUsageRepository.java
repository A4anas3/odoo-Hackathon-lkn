package com.brightloop.discount.repo;

import com.brightloop.discount.entity.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, UUID> {
    List<DiscountUsage> findByDiscountId(UUID discountId);
    List<DiscountUsage> findByCustomerId(UUID customerId);
    long countByDiscountIdAndCustomerId(UUID discountId, UUID customerId);
}
