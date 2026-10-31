package com.brightloop.plan.repo;

import com.brightloop.plan.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByPlanCode(String planCode);

    boolean existsByPlanCode(String planCode);

    java.util.List<Plan> findByIsActiveTrue();

    @Query("SELECT p FROM Plan p WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(p.planCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:interval AS string) IS NULL OR p.billingInterval = :interval) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Plan> searchPlans(
            @Param("search") String search,
            @Param("interval") String interval,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
