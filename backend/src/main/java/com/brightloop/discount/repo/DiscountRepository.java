package com.brightloop.discount.repo;

import com.brightloop.discount.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {

    Optional<Discount> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT d FROM Discount d WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:type AS string) IS NULL OR d.discountType = :type) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive)")
    Page<Discount> findWithFilters(@Param("search") String search,
                                  @Param("type") String type,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);
}
