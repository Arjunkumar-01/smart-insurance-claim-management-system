package com.talentfarm.sicms.payment.repository;

import com.talentfarm.sicms.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByPolicyCustomerId(UUID customerId, Pageable pageable);
    Optional<Payment> findByIdAndPolicyCustomerId(UUID paymentId, UUID customerId);
    boolean existsByPolicyIdAndStatus(UUID policyId, com.talentfarm.sicms.payment.domain.PaymentStatus status);
}