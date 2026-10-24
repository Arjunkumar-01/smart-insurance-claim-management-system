package com.talentfarm.sicms.report.dto.response;

import java.math.BigDecimal;

public record MonthlyRevenueResponse(int year, int month, long paymentCount, BigDecimal completedPremium) { }