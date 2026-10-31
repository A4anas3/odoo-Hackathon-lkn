package com.brightloop.plan.repo;

import com.brightloop.plan.entity.PlanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanProductRepository extends JpaRepository<PlanProduct, UUID> {
    List<PlanProduct> findByPlanId(UUID planId);
}
