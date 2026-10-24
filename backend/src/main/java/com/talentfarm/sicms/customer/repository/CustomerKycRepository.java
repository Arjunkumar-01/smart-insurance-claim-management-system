package com.talentfarm.sicms.customer.repository;

import com.talentfarm.sicms.customer.domain.CustomerKyc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerKycRepository extends JpaRepository<CustomerKyc, UUID> {

    Optional<CustomerKyc> findByCustomerId(UUID customerId);
}