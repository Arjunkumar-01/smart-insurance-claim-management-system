package com.talentfarm.sicms.report.dto.response;

import com.talentfarm.sicms.claim.domain.ClaimStatus;

public record ClaimStatusCount(ClaimStatus status, long count) { }