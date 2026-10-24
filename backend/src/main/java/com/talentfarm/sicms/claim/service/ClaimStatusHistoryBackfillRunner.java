package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.ClaimStatusHistory;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.claim.repository.ClaimStatusHistoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClaimStatusHistoryBackfillRunner implements ApplicationRunner {
    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository historyRepository;

    public ClaimStatusHistoryBackfillRunner(ClaimRepository claimRepository, ClaimStatusHistoryRepository historyRepository) {
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        claimRepository.findAll().forEach(claim -> {
            if (historyRepository.findByClaimIdOrderByChangedAtAscIdAsc(claim.getId()).isEmpty()) {
                ClaimStatusHistory baseline = new ClaimStatusHistory(claim, null, claim.getStatus(), null,
                        "history tracking baseline");
                historyRepository.save(baseline);
            }
        });
    }
}