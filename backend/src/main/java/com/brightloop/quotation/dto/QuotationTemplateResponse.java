package com.brightloop.quotation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationTemplateResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer validityDays;
    private Boolean isActive;
    private List<QuotationTemplateLineDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
}
