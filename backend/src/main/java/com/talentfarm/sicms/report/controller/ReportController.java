package com.talentfarm.sicms.report.controller;

import com.talentfarm.sicms.report.dto.response.AdminOverviewResponse;
import com.talentfarm.sicms.report.dto.response.CustomerReportResponse;
import com.talentfarm.sicms.report.dto.response.MonthlyRevenueResponse;
import com.talentfarm.sicms.report.dto.response.ProductPerformanceResponse;
import com.talentfarm.sicms.report.dto.response.SettlementTrendResponse;
import com.talentfarm.sicms.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) { this.reportService = reportService; }

    @GetMapping("/me/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerReportResponse customerSummary(Authentication authentication) {
        return reportService.customerSummary(authentication);
    }

    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public AdminOverviewResponse adminOverview() { return reportService.adminOverview(); }

    @GetMapping("/admin/revenue/monthly")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<MonthlyRevenueResponse> monthlyRevenue(@RequestParam(defaultValue = "") String year) {
        int requestedYear = year.isBlank() ? Year.now().getValue() : Integer.parseInt(year);
        return reportService.monthlyRevenue(requestedYear);
    }

    @GetMapping("/admin/settlements/trend")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<SettlementTrendResponse> settlementTrend(@RequestParam(defaultValue = "") String year) {
        int requestedYear = year.isBlank() ? Year.now().getValue() : Integer.parseInt(year);
        return reportService.settlementTrend(requestedYear);
    }

    @GetMapping("/admin/products/performance")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<ProductPerformanceResponse> productPerformance() { return reportService.productPerformance(); }
}
