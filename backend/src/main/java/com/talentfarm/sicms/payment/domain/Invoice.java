package com.talentfarm.sicms.payment.domain;

import com.talentfarm.sicms.policy.domain.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;
    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version @Column(nullable = false)
    private Long version;

    protected Invoice() { }
    public Invoice(String invoiceNumber, Policy policy, BigDecimal amount) {
        this.invoiceNumber = invoiceNumber; this.policy = policy; this.amount = amount;
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); issuedAt = now; createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Policy getPolicy() { return policy; }
    public BigDecimal getAmount() { return amount; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}