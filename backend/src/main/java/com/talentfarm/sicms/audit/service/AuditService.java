package com.talentfarm.sicms.audit.service;

import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.domain.AuditLog;
import com.talentfarm.sicms.audit.dto.response.AuditLogPageResponse;
import com.talentfarm.sicms.audit.dto.response.AuditLogResponse;
import com.talentfarm.sicms.audit.repository.AuditLogRepository;
import com.talentfarm.sicms.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {
    private static final int MAX_PAGE_SIZE = 100;
    private final AuditLogRepository repository;
    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional
    public void record(AuditAction action, User actor, String resourceType, UUID resourceId, String description) {
        repository.save(new AuditLog(action, actor, resourceType, resourceId, description));
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse list(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        var result = repository.findAllByOrderByOccurredAtDesc(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt")));
        return new AuditLogPageResponse(result.getContent().stream().map(this::response).toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private AuditLogResponse response(AuditLog log) { return new AuditLogResponse(log.getId(), log.getAction(), log.getActor() == null ? null : log.getActor().getId(), log.getResourceType(), log.getResourceId(), log.getDescription(), log.getOccurredAt()); }
}