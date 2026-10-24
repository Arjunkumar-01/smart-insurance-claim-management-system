package com.talentfarm.sicms.policy.domain;

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
@Table(name = "policy_nominees")
public class PolicyNominee {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false, unique = true)
    private Policy policy;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 80)
    private String relationship;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected PolicyNominee() { }

    public PolicyNominee(Policy policy, String name, String relationship, String contactNumber) {
        this.policy = policy;
        this.name = name;
        this.relationship = relationship;
        this.contactNumber = contactNumber;
    }

    public void update(String name, String relationship, String contactNumber) {
        this.name = name;
        this.relationship = relationship;
        this.contactNumber = contactNumber;
    }

    @PrePersist
    void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Policy getPolicy() { return policy; }
    public String getName() { return name; }
    public String getRelationship() { return relationship; }
    public String getContactNumber() { return contactNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}