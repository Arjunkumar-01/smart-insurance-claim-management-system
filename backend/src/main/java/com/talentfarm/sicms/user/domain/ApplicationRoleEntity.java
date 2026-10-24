package com.talentfarm.sicms.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "application_roles")
public class ApplicationRoleEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private ApplicationRole name;

    protected ApplicationRoleEntity() {
    }

    public ApplicationRoleEntity(UUID id, ApplicationRole name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public ApplicationRole getName() {
        return name;
    }
}