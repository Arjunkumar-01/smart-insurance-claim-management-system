package com.talentfarm.sicms.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "receipts")
public class Receipt {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;
    @Column(name = "receipt_number", nullable = false, unique = true, length = 40)
    private String receiptNumber;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version @Column(nullable = false)
    private Long version;

    protected Receipt() { }
    public Receipt(String receiptNumber, Payment payment) { this.receiptNumber = receiptNumber; this.payment = payment; }
    @PrePersist void onCreate() { Instant now = Instant.now(); issuedAt = now; createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getReceiptNumber() { return receiptNumber; }
    public Payment getPayment() { return payment; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}