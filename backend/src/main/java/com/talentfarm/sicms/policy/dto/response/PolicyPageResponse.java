package com.talentfarm.sicms.policy.dto.response;

import java.util.List;

public record PolicyPageResponse(List<PolicyResponse> policies, int page, int size, long totalElements, int totalPages) { }