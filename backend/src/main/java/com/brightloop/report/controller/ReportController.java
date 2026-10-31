package com.brightloop.report.controller;

import com.brightloop.common.dto.ApiResponse;
import com.brightloop.report.dto.DashboardMetricsResponse;
import com.brightloop.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboardMetrics() {
        DashboardMetricsResponse metrics = reportService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    @GetMapping("/export/subscriptions")
    public ResponseEntity<byte[]> exportSubscriptionsCsv() {
        byte[] csv = reportService.exportSubscriptionsCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "subscriptions_export.csv");
        headers.setContentLength(csv.length);
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @GetMapping("/export/invoices")
    public ResponseEntity<byte[]> exportInvoicesCsv() {
        byte[] csv = reportService.exportInvoicesCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "invoices_export.csv");
        headers.setContentLength(csv.length);
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @GetMapping("/export/payments")
    public ResponseEntity<byte[]> exportPaymentsCsv() {
        byte[] csv = reportService.exportPaymentsCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "payments_export.csv");
        headers.setContentLength(csv.length);
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}
