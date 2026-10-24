package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.ClaimStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ClaimWorkflowService {
    private static final Map<ClaimStatus, Set<ClaimStatus>> TRANSITIONS = Map.of(
            ClaimStatus.SUBMITTED, Set.of(ClaimStatus.UNDER_REVIEW),
            ClaimStatus.UNDER_REVIEW, Set.of(ClaimStatus.DOCUMENTS_REQUIRED, ClaimStatus.APPROVED, ClaimStatus.REJECTED),
            ClaimStatus.DOCUMENTS_REQUIRED, Set.of(ClaimStatus.UNDER_REVIEW),
            ClaimStatus.APPROVED, Set.of(ClaimStatus.SETTLED),
            ClaimStatus.REJECTED, Set.of(),
            ClaimStatus.SETTLED, Set.of());

    public void validate(ClaimStatus current, ClaimStatus next) {
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new IllegalArgumentException("Claim transition is not allowed: " + current + " to " + next);
        }
    }
}