package com.brightloop.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private UUID userId;
    private String userName;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private Instant createdAt;
}
