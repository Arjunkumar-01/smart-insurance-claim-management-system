package com.talentfarm.sicms.claim.domain;

import com.talentfarm.sicms.user.domain.User;
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
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_status_history")
public class ClaimStatusHistory {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(name = "from_status", length = 30)
    @Enumerated(EnumType.STRING)
    private ClaimStatus fromStatus;

    @Column(name = "to_status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ClaimStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(name = "change_reason", nullable = false, length = 500)
    private String changeReason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected ClaimStatusHistory() { }

    public ClaimStatusHistory(Claim claim, ClaimStatus fromStatus, ClaimStatus toStatus, User changedBy, String changeReason) {
        this.claim = claim;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changeReason = changeReason;
    }

    @PrePersist
    void onCreate() { changedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Claim getClaim() { return claim; }
    public ClaimStatus getFromStatus() { return fromStatus; }
    public ClaimStatus getToStatus() { return toStatus; }
    public User getChangedBy() { return changedBy; }
    public String getChangeReason() { return changeReason; }
    public Instant getChangedAt() { return changedAt; }
}