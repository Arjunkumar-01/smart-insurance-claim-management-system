package com.talentfarm.sicms.audit.controller;

import com.talentfarm.sicms.audit.dto.response.AuditLogPageResponse;
import com.talentfarm.sicms.audit.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AuditLogController {
    private final AuditService auditService;
    public AuditLogController(AuditService auditService) { this.auditService = auditService; }
    @GetMapping
    public AuditLogPageResponse list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return auditService.list(page, size); }
}