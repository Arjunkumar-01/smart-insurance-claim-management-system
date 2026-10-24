package com.talentfarm.sicms.payment.service;

import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.payment.domain.Invoice;
import com.talentfarm.sicms.payment.domain.Payment;
import com.talentfarm.sicms.payment.domain.PaymentStatus;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.payment.domain.Receipt;
import com.talentfarm.sicms.payment.dto.request.MakePaymentRequest;
import com.talentfarm.sicms.payment.dto.response.InvoiceResponse;
import com.talentfarm.sicms.payment.dto.response.PaymentPageResponse;
import com.talentfarm.sicms.payment.dto.response.PaymentResponse;
import com.talentfarm.sicms.payment.dto.response.ReceiptDownload;
import com.talentfarm.sicms.payment.dto.response.ReceiptResponse;
import com.talentfarm.sicms.payment.mapper.PaymentMapper;
import com.talentfarm.sicms.payment.repository.InvoiceRepository;
import com.talentfarm.sicms.payment.repository.PaymentRepository;
import com.talentfarm.sicms.payment.repository.ReceiptRepository;
import com.talentfarm.sicms.policy.domain.Policy;
import com.talentfarm.sicms.policy.domain.PolicyStatus;
import com.talentfarm.sicms.policy.repository.PolicyRepository;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {
    private static final int MAX_PAGE_SIZE = 50;
    private final PolicyRepository policyRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReceiptRepository receiptRepository;
    private final PaymentMapper mapper;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final ReceiptPdfService receiptPdfService;

    public PaymentService(PolicyRepository policyRepository, PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository, ReceiptRepository receiptRepository, PaymentMapper mapper,
                          CustomerRepository customerRepository, AuditService auditService,
                          ReceiptPdfService receiptPdfService) {
        this.policyRepository = policyRepository; this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository; this.receiptRepository = receiptRepository; this.mapper = mapper;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
        this.receiptPdfService = receiptPdfService;
    }

    @Transactional
    public PaymentResponse pay(Authentication authentication, MakePaymentRequest request) {
        UUID customerId = currentCustomerId(authentication);
        Policy policy = policyRepository.findByIdAndCustomerId(request.policyId(), customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy was not found"));
        if (policy.getStatus() == PolicyStatus.CANCELLED) throw new IllegalArgumentException("Cancelled policies cannot be paid");
        if (paymentRepository.existsByPolicyIdAndStatus(policy.getId(), PaymentStatus.COMPLETED)) {
            throw new DuplicateResourceException("Policy premium has already been paid");
        }
        Invoice invoice = invoiceRepository.saveAndFlush(new Invoice("INV-" + UUID.randomUUID().toString().toUpperCase(), policy, policy.getPremiumAmount()));
        Payment payment = paymentRepository.saveAndFlush(new Payment("PAY-" + UUID.randomUUID().toString().toUpperCase(), policy, invoice,
                policy.getPremiumAmount(), request.paymentMethod(), PaymentStatus.COMPLETED, "SIM-" + UUID.randomUUID().toString().toUpperCase()));
        Receipt receipt = receiptRepository.saveAndFlush(new Receipt("REC-" + UUID.randomUUID().toString().toUpperCase(), payment));
        auditService.record(AuditAction.PREMIUM_PAYMENT_COMPLETED, null, "PAYMENT", payment.getId(), "Premium payment completed");
        return mapper.toResponse(payment, invoice, receipt);
    }

    @Transactional(readOnly = true)
    public PaymentPageResponse history(Authentication authentication, int page, int size) {
        validatePage(page, size);
        Page<Payment> payments = paymentRepository.findByPolicyCustomerId(currentCustomerId(authentication),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PaymentPageResponse(payments.getContent().stream().map(this::response).toList(), payments.getNumber(), payments.getSize(), payments.getTotalElements(), payments.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Authentication authentication, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndPolicyCustomerId(paymentId, currentCustomerId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Payment was not found"));
        return response(payment);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse invoice(Authentication authentication, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndPolicyCustomerId(paymentId, currentCustomerId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Payment was not found"));
        return mapper.toInvoice(payment.getInvoice());
    }

    @Transactional(readOnly = true)
    public ReceiptResponse receipt(Authentication authentication, UUID paymentId) {
        Receipt receipt = receiptRepository.findByPaymentIdAndPaymentPolicyCustomerId(paymentId, currentCustomerId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Receipt was not found"));
        return mapper.toReceipt(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptDownload downloadReceipt(Authentication authentication, UUID paymentId) {
        Receipt receipt = receiptRepository.findByPaymentIdAndPaymentPolicyCustomerId(paymentId, currentCustomerId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Receipt was not found"));
        return new ReceiptDownload(receiptPdfService.generate(receipt), "sicms-receipt-" + receipt.getReceiptNumber() + ".pdf");
    }

    private PaymentResponse response(Payment payment) { return mapper.toResponse(payment, payment.getInvoice(), payment.getInvoice() == null ? null : receiptRepository.findByPaymentIdAndPaymentPolicyCustomerId(payment.getId(), payment.getPolicy().getCustomer().getId()).orElse(null)); }
    private UUID currentCustomerId(Authentication authentication) {
        return customerRepository.findByUserId(((CustomUserDetails) authentication.getPrincipal()).getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found")).getId();
    }
    private void validatePage(int page, int size) { if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 50"); }
}