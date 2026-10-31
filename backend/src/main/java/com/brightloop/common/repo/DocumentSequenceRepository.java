package com.brightloop.common.repo;

import com.brightloop.common.entity.DocumentSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DocumentSequence d WHERE d.documentType = :docType AND d.financialYear = :fy")
    Optional<DocumentSequence> findByDocumentTypeAndFinancialYearForUpdate(
            @Param("docType") String docType,
            @Param("fy") String fy
    );
}
