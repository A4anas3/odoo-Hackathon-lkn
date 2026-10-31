package com.brightloop.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "document_sequences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"document_type", "financial_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_type", length = 30, nullable = false)
    private String documentType;

    @Column(name = "financial_year", length = 20, nullable = false)
    private String financialYear;

    @Column(name = "prefix", length = 30, nullable = false)
    private String prefix;

    @Column(name = "next_number", nullable = false)
    private Long nextNumber;
}
