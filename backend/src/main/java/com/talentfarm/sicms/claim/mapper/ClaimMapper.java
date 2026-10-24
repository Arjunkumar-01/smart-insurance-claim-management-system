package com.talentfarm.sicms.claim.mapper;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.claim.dto.response.ClaimResponse;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {
    public ClaimResponse toResponse(Claim claim) {
        var policy = claim.getPolicy();
        return new ClaimResponse(claim.getId(), claim.getClaimNumber(), policy.getId(), policy.getPolicyNumber(),
            claim.getClaimAmount(), policy.getCoverageAmount(), claim.getClaimType(), claim.getIncidentDate(),
            claim.getDescription(), claim.getStatus(), claim.getSubmittedAt(), claim.getCreatedAt(), claim.getUpdatedAt());
    }
}