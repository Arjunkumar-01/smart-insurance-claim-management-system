package com.talentfarm.sicms.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "customer_kyc")
public class CustomerKyc {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(name = "identity_type", nullable = false, length = 50)
    private String identityType;

    @Column(name = "identity_number", nullable = false, length = 100)
    private String identityNumber;

    @Column(name = "kyc_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected CustomerKyc() {
    }

    public CustomerKyc(Customer customer, String identityType, String identityNumber) {
        this.customer = customer;
        this.identityType = identityType;
        this.identityNumber = identityNumber;
        this.kycStatus = KycStatus.PENDING;
    }

    public void update(String identityType, String identityNumber) {
        this.identityType = identityType;
        this.identityNumber = identityNumber;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getIdentityType() { return identityType; }
    public String getIdentityNumber() { return identityNumber; }
    public KycStatus getKycStatus() { return kycStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}