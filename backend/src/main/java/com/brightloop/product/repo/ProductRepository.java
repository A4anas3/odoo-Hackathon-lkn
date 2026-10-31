package com.brightloop.product.repo;

import com.brightloop.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "(CAST(:search AS string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:productType AS string) IS NULL OR p.productType = :productType) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("productType") String productType,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
