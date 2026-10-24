package com.talentfarm.sicms.claim.repository;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.claim.domain.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Page<Claim> findByPolicyCustomerId(UUID customerId, Pageable pageable);
    Optional<Claim> findByIdAndPolicyCustomerId(UUID claimId, UUID customerId);
    Page<Claim> findByStatusIn(List<ClaimStatus> statuses, Pageable pageable);
}