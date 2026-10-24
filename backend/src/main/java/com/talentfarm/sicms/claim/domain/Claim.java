package com.talentfarm.sicms.claim.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 40)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "claim_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "claim_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ClaimType claimType;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Claim() { }

    public Claim(String claimNumber, Policy policy, BigDecimal claimAmount, ClaimType claimType,
                 LocalDate incidentDate, String description) {
        this.claimNumber = claimNumber;
        this.policy = policy;
        this.claimAmount = claimAmount;
        this.claimType = claimType;
        this.incidentDate = incidentDate;
        this.description = description;
        this.status = ClaimStatus.SUBMITTED;
    }

    public void transitionTo(ClaimStatus nextStatus) {
        this.status = nextStatus;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        submittedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getClaimNumber() { return claimNumber; }
    public Policy getPolicy() { return policy; }
    public BigDecimal getClaimAmount() { return claimAmount; }
    public ClaimType getClaimType() { return claimType; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public String getDescription() { return description; }
    public ClaimStatus getStatus() { return status; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}