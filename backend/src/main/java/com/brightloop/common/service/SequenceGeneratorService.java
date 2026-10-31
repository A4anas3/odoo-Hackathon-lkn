package com.brightloop.common.service;

import com.brightloop.common.entity.DocumentSequence;
import com.brightloop.common.repo.DocumentSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SequenceGeneratorService {

    private final DocumentSequenceRepository sequenceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public String getNextSequence(String documentType) {
        String fy = getCurrentFinancialYear();
        String prefix = getPrefixForType(documentType);

        DocumentSequence sequence = sequenceRepository.findByDocumentTypeAndFinancialYearForUpdate(documentType, fy)
                .orElseGet(() -> {
                    DocumentSequence newSeq = DocumentSequence.builder()
                            .documentType(documentType)
                            .financialYear(fy)
                            .prefix(prefix)
                            .nextNumber(1L)
                            .build();
                    return sequenceRepository.saveAndFlush(newSeq);
                });

        long currentNumber = sequence.getNextNumber();
        sequence.setNextNumber(currentNumber + 1);
        sequenceRepository.save(sequence);

        return String.format("%s%03d", sequence.getPrefix(), currentNumber);
    }

    public String generateInvoiceNumber() {
        return getNextSequence("INVOICE");
    }

    public String generateQuotationNumber() {
        return getNextSequence("QUOTATION");
    }

    public String generateSubscriptionNumber() {
        return getNextSequence("SUBSCRIPTION");
    }

    public String generatePaymentNumber() {
        return getNextSequence("PAYMENT");
    }

    public String generateCustomerCode() {
        return getNextSequence("CUSTOMER");
    }

    private String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        if (month >= 4) {
            return String.format("%d-%02d", year, (year + 1) % 100);
        } else {
            return String.format("%d-%02d", year - 1, year % 100);
        }
    }

    private String getPrefixForType(String documentType) {
        int year = LocalDate.now().getYear();
        return switch (documentType.toUpperCase()) {
            case "INVOICE" -> "INV/" + year + "/";
            case "QUOTATION" -> "QTN/" + year + "/";
            case "SUBSCRIPTION" -> "SUB/" + year + "/";
            case "PAYMENT" -> "PAY/" + year + "/";
            case "CUSTOMER" -> "CUST-";
            default -> "DOC/" + year + "/";
        };
    }
}
