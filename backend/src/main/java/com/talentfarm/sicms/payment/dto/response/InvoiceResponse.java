package com.talentfarm.sicms.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(UUID invoiceId, String invoiceNumber, UUID policyId, BigDecimal amount, Instant issuedAt) { }