package com.brightloop.audit.service;

import com.brightloop.audit.dto.AuditLogResponse;
import com.brightloop.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditService {
    void logAction(String entityType, UUID entityId, String action, UUID userId, String oldValues, String newValues, String ipAddress);
    PageResponse<AuditLogResponse> listAuditLogs(String entityType, UUID entityId, String action, Pageable pageable);
}
