package com.talentfarm.sicms.payment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(UUID receiptId, String receiptNumber, UUID paymentId, Instant issuedAt) { }