package com.talentfarm.sicms.payment.dto.response;

import com.talentfarm.sicms.payment.domain.PaymentMethod;
import com.talentfarm.sicms.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID paymentId, String paymentNumber, UUID policyId, BigDecimal amount,
                              PaymentMethod paymentMethod, PaymentStatus status, String transactionReference,
                              Instant paidAt, InvoiceResponse invoice, ReceiptResponse receipt) { }