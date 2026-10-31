package com.brightloop.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanProductDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productCode;
    private Integer defaultQuantity;
    private Boolean isMandatory;
}
