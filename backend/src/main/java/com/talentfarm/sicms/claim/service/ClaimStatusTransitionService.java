package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.claim.domain.ClaimStatusHistory;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.claim.repository.ClaimStatusHistoryRepository;
import com.talentfarm.sicms.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimStatusTransitionService {
    private final ClaimWorkflowService workflow;
    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository historyRepository;

    public ClaimStatusTransitionService(ClaimWorkflowService workflow, ClaimRepository claimRepository,
                                        ClaimStatusHistoryRepository historyRepository) {
        this.workflow = workflow;
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void transition(Claim claim, ClaimStatus nextStatus, User actor, String reason) {
        ClaimStatus current = claim.getStatus();
        workflow.validate(current, nextStatus);
        claim.transitionTo(nextStatus);
        claimRepository.saveAndFlush(claim);
        historyRepository.saveAndFlush(new ClaimStatusHistory(claim, current, nextStatus, actor, reason));
    }

    @Transactional
    public void recordInitialSubmission(Claim claim, User actor) {
        historyRepository.saveAndFlush(new ClaimStatusHistory(claim, null, ClaimStatus.SUBMITTED, actor, "initial submission"));
    }
}