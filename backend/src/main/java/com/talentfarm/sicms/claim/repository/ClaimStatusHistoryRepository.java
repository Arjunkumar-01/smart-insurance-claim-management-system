package com.talentfarm.sicms.claim.repository;

import com.talentfarm.sicms.claim.domain.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, UUID> {
    List<ClaimStatusHistory> findByClaimIdOrderByChangedAtAscIdAsc(UUID claimId);
}