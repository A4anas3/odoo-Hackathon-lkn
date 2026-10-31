package com.brightloop.quotation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotNull
    @Builder.Default
    private Integer validityDays = 30;

    @Builder.Default
    private Boolean isActive = true;

    private List<QuotationTemplateLineDto> lines;
}
