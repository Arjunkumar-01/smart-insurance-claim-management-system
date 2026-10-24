package com.talentfarm.sicms.payment.controller;

import com.talentfarm.sicms.payment.dto.request.MakePaymentRequest;
import com.talentfarm.sicms.payment.dto.response.InvoiceResponse;
import com.talentfarm.sicms.payment.dto.response.PaymentPageResponse;
import com.talentfarm.sicms.payment.dto.response.PaymentResponse;
import com.talentfarm.sicms.payment.dto.response.ReceiptResponse;
import com.talentfarm.sicms.payment.dto.response.ReceiptDownload;
import com.talentfarm.sicms.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasRole('CUSTOMER')")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(Authentication authentication, @Valid @RequestBody MakePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(authentication, request));
    }

    @GetMapping("/me")
    public PaymentPageResponse history(Authentication authentication, @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return paymentService.history(authentication, page, size);
    }

    @GetMapping("/me/{paymentId}")
    public PaymentResponse get(Authentication authentication, @PathVariable UUID paymentId) { return paymentService.get(authentication, paymentId); }

    @GetMapping("/me/{paymentId}/invoice")
    public InvoiceResponse invoice(Authentication authentication, @PathVariable UUID paymentId) { return paymentService.invoice(authentication, paymentId); }

    @GetMapping("/me/{paymentId}/receipt")
    public ReceiptResponse receipt(Authentication authentication, @PathVariable UUID paymentId) { return paymentService.receipt(authentication, paymentId); }

    @GetMapping(value = "/me/{paymentId}/receipt/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadReceipt(Authentication authentication, @PathVariable UUID paymentId) {
        ReceiptDownload download = paymentService.downloadReceipt(authentication, paymentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(download.content());
    }
}