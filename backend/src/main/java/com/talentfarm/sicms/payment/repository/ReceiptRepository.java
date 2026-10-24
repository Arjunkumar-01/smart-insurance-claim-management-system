package com.talentfarm.sicms.payment.repository;

import com.talentfarm.sicms.payment.domain.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByPaymentIdAndPaymentPolicyCustomerId(UUID paymentId, UUID customerId);
}