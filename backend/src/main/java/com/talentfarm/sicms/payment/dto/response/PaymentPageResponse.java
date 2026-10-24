package com.talentfarm.sicms.payment.dto.response;

import java.util.List;

public record PaymentPageResponse(List<PaymentResponse> payments, int page, int size, long totalElements, int totalPages) { }