package com.talentfarm.sicms.claim.domain;

import com.talentfarm.sicms.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "claim_settlements")
public class ClaimSettlement {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @Column(name = "settlement_number", nullable = false, unique = true, length = 40)
    private String settlementNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @Column(name = "settlement_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal settlementAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settled_by_user_id", nullable = false)
    private User settledBy;

    @Column(name = "settled_at", nullable = false, updatable = false)
    private Instant settledAt;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version @Column(nullable = false)
    private Long version;

    protected ClaimSettlement() { }

    public ClaimSettlement(String settlementNumber, Claim claim, BigDecimal settlementAmount, User settledBy, String notes) {
        this.settlementNumber = settlementNumber;
        this.claim = claim;
        this.settlementAmount = settlementAmount;
        this.settledBy = settledBy;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() { Instant now = Instant.now(); settledAt = now; createdAt = now; updatedAt = now; }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getSettlementNumber() { return settlementNumber; }
    public Claim getClaim() { return claim; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public User getSettledBy() { return settledBy; }
    public Instant getSettledAt() { return settledAt; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}