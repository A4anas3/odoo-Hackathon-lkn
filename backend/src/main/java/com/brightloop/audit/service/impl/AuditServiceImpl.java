package com.brightloop.audit.service.impl;

import com.brightloop.audit.dto.AuditLogResponse;
import com.brightloop.audit.entity.AuditLog;
import com.brightloop.audit.repo.AuditLogRepository;
import com.brightloop.audit.service.AuditService;
import com.brightloop.common.dto.PageResponse;
import com.brightloop.user.entity.User;
import com.brightloop.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void logAction(String entityType, UUID entityId, String action, UUID userId, String oldValues, String newValues, String ipAddress) {
        AuditLog logEntry = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .userId(userId)
                .oldValues(oldValues)
                .newValues(newValues)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(logEntry);
        log.debug("Recorded audit log for {} {} - action: {}", entityType, entityId, action);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAuditLogs(String entityType, UUID entityId, String action, Pageable pageable) {
        String cleanType = (entityType != null && !entityType.trim().isEmpty()) ? entityType.trim() : null;
        String cleanAction = (action != null && !action.trim().isEmpty()) ? action.trim() : null;

        Page<AuditLog> page = auditLogRepository.findWithFilters(cleanType, entityId, cleanAction, pageable);
        List<AuditLogResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<AuditLogResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    private AuditLogResponse mapToResponse(AuditLog a) {
        String userName = null;
        if (a.getUserId() != null) {
            userName = userRepository.findById(a.getUserId()).map(User::getFullName).orElse(null);
        }
        return AuditLogResponse.builder()
                .id(a.getId())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .action(a.getAction())
                .userId(a.getUserId())
                .userName(userName)
                .oldValues(a.getOldValues())
                .newValues(a.getNewValues())
                .ipAddress(a.getIpAddress())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
