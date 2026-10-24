package com.talentfarm.sicms.policy.repository;

import com.talentfarm.sicms.policy.domain.PolicyNominee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PolicyNomineeRepository extends JpaRepository<PolicyNominee, UUID> {
    Optional<PolicyNominee> findByPolicyId(UUID policyId);
}