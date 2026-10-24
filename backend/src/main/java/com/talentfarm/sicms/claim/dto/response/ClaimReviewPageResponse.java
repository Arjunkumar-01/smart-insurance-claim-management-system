package com.talentfarm.sicms.claim.dto.response;

import java.util.List;

public record ClaimReviewPageResponse(List<ClaimReviewDetailResponse> claims, int page, int size,
                                      long totalElements, int totalPages) { }