package com.talentfarm.sicms.policy.repository;

import com.talentfarm.sicms.policy.domain.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
    Page<Policy> findByCustomerId(UUID customerId, Pageable pageable);
    Optional<Policy> findByIdAndCustomerId(UUID id, UUID customerId);
}