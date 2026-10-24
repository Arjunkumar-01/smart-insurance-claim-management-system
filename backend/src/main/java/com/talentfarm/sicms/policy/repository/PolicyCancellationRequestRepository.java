package com.talentfarm.sicms.policy.repository;

import com.talentfarm.sicms.policy.domain.CancellationStatus;
import com.talentfarm.sicms.policy.domain.PolicyCancellationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyCancellationRequestRepository extends JpaRepository<PolicyCancellationRequest, UUID> {
    boolean existsByPolicyIdAndStatus(UUID policyId, CancellationStatus status);
    Optional<PolicyCancellationRequest> findByIdAndPolicyCustomerId(UUID id, UUID customerId);
    List<PolicyCancellationRequest> findByStatusOrderByRequestedAtAsc(CancellationStatus status);
}