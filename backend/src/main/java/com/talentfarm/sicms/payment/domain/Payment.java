package com.talentfarm.sicms.payment.domain;

import com.talentfarm.sicms.policy.domain.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;
    @Column(name = "payment_number", nullable = false, unique = true, length = 40)
    private String paymentNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false, unique = true)
    private Invoice invoice;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 60)
    private String transactionReference;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version @Column(nullable = false)
    private Long version;

    protected Payment() { }
    public Payment(String paymentNumber, Policy policy, Invoice invoice, BigDecimal amount, PaymentMethod method,
                   PaymentStatus status, String transactionReference) {
        this.paymentNumber = paymentNumber; this.policy = policy; this.invoice = invoice; this.amount = amount;
        this.paymentMethod = method; this.status = status; this.transactionReference = transactionReference;
        this.paidAt = Instant.now();
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getPaymentNumber() { return paymentNumber; }
    public Policy getPolicy() { return policy; }
    public Invoice getInvoice() { return invoice; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionReference() { return transactionReference; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}