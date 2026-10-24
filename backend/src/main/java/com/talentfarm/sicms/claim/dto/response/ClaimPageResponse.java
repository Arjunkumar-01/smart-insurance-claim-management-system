package com.talentfarm.sicms.claim.dto.response;

import java.util.List;

public record ClaimPageResponse(List<ClaimResponse> claims, int page, int size, long totalElements, int totalPages) { }