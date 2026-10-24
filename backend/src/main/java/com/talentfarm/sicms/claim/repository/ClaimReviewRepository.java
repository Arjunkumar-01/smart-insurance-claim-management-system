package com.talentfarm.sicms.claim.repository;

import com.talentfarm.sicms.claim.domain.ClaimReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimReviewRepository extends JpaRepository<ClaimReview, UUID> {
    List<ClaimReview> findByClaimIdOrderByCreatedAtAsc(UUID claimId);
}