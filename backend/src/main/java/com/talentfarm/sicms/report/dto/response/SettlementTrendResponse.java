package com.talentfarm.sicms.report.dto.response;

import java.math.BigDecimal;

public record SettlementTrendResponse(int year, int month, long settlementCount, BigDecimal settlementAmount) { }