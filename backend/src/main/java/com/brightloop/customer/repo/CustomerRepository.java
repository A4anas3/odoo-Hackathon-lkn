package com.brightloop.customer.repo;

import com.brightloop.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByUserId(UUID userId);

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByEmail(String email);

    boolean existsByCustomerCode(String customerCode);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.deletedAt IS NULL AND " +
           "(CAST(:search AS string) IS NULL OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
           "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:status AS string) IS NULL OR c.status = :status) AND " +
           "(CAST(:customerType AS string) IS NULL OR c.customerType = :customerType)")
    Page<Customer> searchCustomers(
            @Param("search") String search,
            @Param("status") String status,
            @Param("customerType") String customerType,
            Pageable pageable
    );
}
