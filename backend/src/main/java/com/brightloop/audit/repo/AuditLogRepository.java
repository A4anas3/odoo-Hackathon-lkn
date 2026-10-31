package com.brightloop.audit.repo;

import com.brightloop.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(CAST(:entityType AS string) IS NULL OR a.entityType = :entityType) AND " +
           "(:entityId IS NULL OR a.entityId = :entityId) AND " +
           "(CAST(:action AS string) IS NULL OR a.action = :action)")
    Page<AuditLog> findWithFilters(@Param("entityType") String entityType,
                                  @Param("entityId") UUID entityId,
                                  @Param("action") String action,
                                  Pageable pageable);
}
