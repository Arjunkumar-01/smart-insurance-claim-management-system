package com.talentfarm.sicms.claim.repository;

import com.talentfarm.sicms.claim.domain.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, UUID> {
    List<ClaimDocument> findByClaimIdOrderByUploadedAtAsc(UUID claimId);
    Optional<ClaimDocument> findByIdAndClaimId(UUID documentId, UUID claimId);
}