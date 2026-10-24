package com.talentfarm.sicms.audit.dto.response;

import com.talentfarm.sicms.audit.domain.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(UUID auditId, AuditAction action, UUID actorId, String resourceType,
                               UUID resourceId, String description, Instant occurredAt) { }