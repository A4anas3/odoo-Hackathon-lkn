package com.brightloop.report.service;

import com.brightloop.report.dto.DashboardMetricsResponse;

public interface ReportService {
    DashboardMetricsResponse getDashboardMetrics();
    byte[] exportSubscriptionsCsv();
    byte[] exportInvoicesCsv();
    byte[] exportPaymentsCsv();
}
