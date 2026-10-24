package com.talentfarm.sicms.payment.repository;

import com.talentfarm.sicms.payment.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByIdAndPolicyCustomerId(UUID invoiceId, UUID customerId);
}