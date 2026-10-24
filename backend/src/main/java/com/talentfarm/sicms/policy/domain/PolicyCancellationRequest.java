package com.talentfarm.sicms.policy.domain;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_cancellation_requests")
public class PolicyCancellationRequest {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CancellationStatus status;

    @Column(length = 1000)
    private String reason;

    @Column(name = "decision_comment", length = 1000)
    private String decisionComment;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected PolicyCancellationRequest() { }

    public PolicyCancellationRequest(Policy policy, String reason) {
        this.policy = policy;
        this.reason = reason;
        this.status = CancellationStatus.PENDING;
    }

    public void decide(CancellationStatus status, String decisionComment) {
        this.status = status;
        this.decisionComment = decisionComment;
        this.decidedAt = Instant.now();
    }

    @PrePersist
    void onCreate() { requestedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { }

    public UUID getId() { return id; }
    public Policy getPolicy() { return policy; }
    public CancellationStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getDecisionComment() { return decisionComment; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public Long getVersion() { return version; }
}