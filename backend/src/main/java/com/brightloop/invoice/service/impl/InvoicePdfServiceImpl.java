package com.brightloop.invoice.service.impl;

import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.invoice.entity.Invoice;
import com.brightloop.invoice.entity.InvoiceLine;
import com.brightloop.invoice.entity.InvoiceTaxLine;
import com.brightloop.invoice.repo.InvoiceRepository;
import com.brightloop.invoice.service.InvoicePdfService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findWithDetailsById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        return generateInvoicePdf(invoice);
    }

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(113, 75, 103)); // Odoo Purple #714B67
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // Company Header
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60f, 40f});

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.addElement(new Paragraph("Brightloop ERP", brandFont));
            leftCell.addElement(new Paragraph("Subscription & Recurring Billing Platform", smallFont));
            leftCell.addElement(new Paragraph("123 Business Avenue, Cyber City, Lucknow, UP", smallFont));
            leftCell.addElement(new Paragraph("GSTIN: 09AAACB1234F1Z8", smallFont));
            headerTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invTitle = new Paragraph("TAX INVOICE", titleFont);
            invTitle.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(invTitle);

            Paragraph invNum = new Paragraph(invoice.getInvoiceNumber(), boldFont);
            invNum.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(invNum);

            Paragraph invStatus = new Paragraph("Status: " + invoice.getStatus(), normalFont);
            invStatus.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(invStatus);

            headerTable.addCell(rightCell);
            document.add(headerTable);

            document.add(new Paragraph(" "));

            // Customer Bill-To and Invoice Meta Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{55f, 45f});

            PdfPCell billToCell = new PdfPCell();
            billToCell.setBorder(PdfPCell.NO_BORDER);
            billToCell.addElement(new Paragraph("BILL TO:", boldFont));
            if (invoice.getCustomer() != null) {
                billToCell.addElement(new Paragraph(invoice.getCustomer().getName(), boldFont));
                if (invoice.getCustomer().getEmail() != null) {
                    billToCell.addElement(new Paragraph(invoice.getCustomer().getEmail(), normalFont));
                }
                if (invoice.getCustomer().getAddress() != null) {
                    billToCell.addElement(new Paragraph(invoice.getCustomer().getAddress() + ", " +
                            (invoice.getCustomer().getCity() != null ? invoice.getCustomer().getCity() : "") + ", " +
                            (invoice.getCustomer().getState() != null ? invoice.getCustomer().getState() : "") + " " +
                            (invoice.getCustomer().getPostalCode() != null ? invoice.getCustomer().getPostalCode() : ""), normalFont));
                }
                if (invoice.getCustomer().getGstin() != null) {
                    billToCell.addElement(new Paragraph("GSTIN: " + invoice.getCustomer().getGstin(), normalFont));
                }
            }
            metaTable.addCell(billToCell);

            PdfPCell datesCell = new PdfPCell();
            datesCell.setBorder(PdfPCell.NO_BORDER);
            datesCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPTable innerDates = new PdfPTable(2);
            innerDates.setWidthPercentage(100);
            addMetaRow(innerDates, "Invoice Date:", invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : "-", boldFont, normalFont);
            addMetaRow(innerDates, "Due Date:", invoice.getDueDate() != null ? invoice.getDueDate().toString() : "-", boldFont, normalFont);
            if (invoice.getPaymentTerms() != null) {
                addMetaRow(innerDates, "Terms:", invoice.getPaymentTerms(), boldFont, normalFont);
            }
            if (invoice.getBillingPeriodStart() != null && invoice.getBillingPeriodEnd() != null) {
                addMetaRow(innerDates, "Period:", invoice.getBillingPeriodStart() + " to " + invoice.getBillingPeriodEnd(), boldFont, normalFont);
            }
            datesCell.addElement(innerDates);
            metaTable.addCell(datesCell);

            document.add(metaTable);
            document.add(new Paragraph(" "));

            // Items Table
            PdfPTable itemsTable = new PdfPTable(5);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{45f, 12f, 15f, 13f, 15f});

            Color bannerBg = new Color(113, 75, 103);
            addTableHeader(itemsTable, "Item & Description", bannerBg, headerFont, Element.ALIGN_LEFT);
            addTableHeader(itemsTable, "Qty", bannerBg, headerFont, Element.ALIGN_CENTER);
            addTableHeader(itemsTable, "Unit Price", bannerBg, headerFont, Element.ALIGN_RIGHT);
            addTableHeader(itemsTable, "Tax Rate", bannerBg, headerFont, Element.ALIGN_RIGHT);
            addTableHeader(itemsTable, "Amount (INR)", bannerBg, headerFont, Element.ALIGN_RIGHT);

            if (invoice.getLines() != null) {
                for (InvoiceLine line : invoice.getLines()) {
                    addTableCell(itemsTable, line.getDescription(), normalFont, Element.ALIGN_LEFT);
                    addTableCell(itemsTable, String.valueOf(line.getQuantity()), normalFont, Element.ALIGN_CENTER);
                    addTableCell(itemsTable, "₹ " + line.getUnitPrice().setScale(2).toString(), normalFont, Element.ALIGN_RIGHT);
                    addTableCell(itemsTable, (line.getTaxRate() != null ? line.getTaxRate().multiply(java.math.BigDecimal.valueOf(100)).setScale(1) : "0") + "%", normalFont, Element.ALIGN_RIGHT);
                    addTableCell(itemsTable, "₹ " + line.getLineTotal().setScale(2).toString(), normalFont, Element.ALIGN_RIGHT);
                }
            }

            document.add(itemsTable);
            document.add(new Paragraph(" "));

            // Summary Table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(45);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addSummaryRow(summaryTable, "Subtotal:", "₹ " + invoice.getSubtotal().setScale(2).toString(), boldFont, normalFont);
            if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                addSummaryRow(summaryTable, "Discount:", "- ₹ " + invoice.getDiscountAmount().setScale(2).toString(), boldFont, normalFont);
            }

            // Tax breakdown
            if (invoice.getTaxLines() != null && !invoice.getTaxLines().isEmpty()) {
                for (InvoiceTaxLine taxLine : invoice.getTaxLines()) {
                    addSummaryRow(summaryTable, taxLine.getTaxName() + ":", "₹ " + taxLine.getTaxAmount().setScale(2).toString(), boldFont, normalFont);
                }
            } else {
                addSummaryRow(summaryTable, "Tax Amount:", "₹ " + invoice.getTaxAmount().setScale(2).toString(), boldFont, normalFont);
            }

            addSummaryRow(summaryTable, "Total Amount:", "₹ " + invoice.getTotalAmount().setScale(2).toString(), boldFont, boldFont);
            addSummaryRow(summaryTable, "Paid Amount:", "₹ " + invoice.getPaidAmount().setScale(2).toString(), boldFont, normalFont);
            addSummaryRow(summaryTable, "Balance Due:", "₹ " + invoice.getBalanceDue().setScale(2).toString(), boldFont, boldFont);

            document.add(summaryTable);

            // Notes
            if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Notes & Terms:", boldFont));
                document.add(new Paragraph(invoice.getNotes(), smallFont));
            }

            document.close();
        } catch (DocumentException e) {
            log.error("Error generating PDF for invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }

        return out.toByteArray();
    }

    private void addMetaRow(PdfPTable table, String label, String value, Font labelFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valFont));
        c2.setBorder(PdfPCell.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private void addTableHeader(PdfPTable table, String text, Color bg, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(6f);
        cell.setBorderColor(new Color(200, 200, 200));
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(5f);
        cell.setBorderColor(new Color(225, 225, 225));
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setPadding(4f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valFont));
        c2.setBorder(PdfPCell.NO_BORDER);
        c2.setPadding(4f);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }
}
