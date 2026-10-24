package com.talentfarm.sicms.audit.dto.response;

import java.util.List;

public record AuditLogPageResponse(List<AuditLogResponse> logs, int page, int size, long totalElements, int totalPages) { }