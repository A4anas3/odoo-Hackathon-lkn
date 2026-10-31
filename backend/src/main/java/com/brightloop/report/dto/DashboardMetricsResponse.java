package com.brightloop.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsResponse {
    private BigDecimal mrr;
    private BigDecimal arr;
    private BigDecimal totalRevenue;
    private BigDecimal outstandingBalance;
    private long activeSubscriptionsCount;
    private long pausedSubscriptionsCount;
    private long closedSubscriptionsCount;
    private long totalCustomersCount;
    private long openInvoicesCount;
    private long overdueInvoicesCount;
    private Map<String, Long> subscriptionsByStatus;
    private List<RevenueTrendPoint> revenueTrends;
    private List<PlanSubscriptionMetric> planDistribution;
    private List<CustomerRevenueMetric> topCustomers;
}
