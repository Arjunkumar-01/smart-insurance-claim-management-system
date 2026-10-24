package com.talentfarm.sicms.audit.domain;

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
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;
    @Column(nullable = false, length = 50) @Enumerated(EnumType.STRING)
    private AuditAction action;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;
    @Column(name = "resource_type", length = 50)
    private String resourceType;
    @Column(name = "resource_id")
    private UUID resourceId;
    @Column(nullable = false, length = 500)
    private String description;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditLog() { }
    public AuditLog(AuditAction action, User actor, String resourceType, UUID resourceId, String description) {
        this.action = action; this.actor = actor; this.resourceType = resourceType; this.resourceId = resourceId; this.description = description;
    }
    @PrePersist void onCreate() { occurredAt = Instant.now(); }
    public UUID getId() { return id; }
    public AuditAction getAction() { return action; }
    public User getActor() { return actor; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public String getDescription() { return description; }
    public Instant getOccurredAt() { return occurredAt; }
}