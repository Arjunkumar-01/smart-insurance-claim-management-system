package com.talentfarm.sicms.claim.repository;

import com.talentfarm.sicms.claim.domain.ClaimSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClaimSettlementRepository extends JpaRepository<ClaimSettlement, UUID> {
    boolean existsByClaimId(UUID claimId);
    Optional<ClaimSettlement> findByClaimId(UUID claimId);
}