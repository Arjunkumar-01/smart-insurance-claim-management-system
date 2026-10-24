package com.talentfarm.sicms.payment.mapper;

import com.talentfarm.sicms.payment.domain.Invoice;
import com.talentfarm.sicms.payment.domain.Payment;
import com.talentfarm.sicms.payment.domain.Receipt;
import com.talentfarm.sicms.payment.dto.response.InvoiceResponse;
import com.talentfarm.sicms.payment.dto.response.PaymentResponse;
import com.talentfarm.sicms.payment.dto.response.ReceiptResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment, Invoice invoice, Receipt receipt) {
        return new PaymentResponse(payment.getId(), payment.getPaymentNumber(), payment.getPolicy().getId(), payment.getAmount(),
                payment.getPaymentMethod(), payment.getStatus(), payment.getTransactionReference(), payment.getPaidAt(),
                toInvoice(invoice), toReceipt(receipt));
    }
    public InvoiceResponse toInvoice(Invoice invoice) {
        return invoice == null ? null : new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getPolicy().getId(), invoice.getAmount(), invoice.getIssuedAt());
    }
    public ReceiptResponse toReceipt(Receipt receipt) {
        return receipt == null ? null : new ReceiptResponse(receipt.getId(), receipt.getReceiptNumber(), receipt.getPayment().getId(), receipt.getIssuedAt());
    }
}