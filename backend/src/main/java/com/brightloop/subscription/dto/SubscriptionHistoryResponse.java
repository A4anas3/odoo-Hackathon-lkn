package com.brightloop.subscription.dto;

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
public class SubscriptionHistoryResponse {
    private UUID id;
    private String fromStatus;
    private String toStatus;
    private UUID changedBy;
    private String changedByName;
    private String reason;
    private Instant createdAt;
}
