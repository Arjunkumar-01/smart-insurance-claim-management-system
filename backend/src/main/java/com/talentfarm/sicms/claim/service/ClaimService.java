package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.claim.dto.request.SubmitClaimRequest;
import com.talentfarm.sicms.claim.dto.response.ClaimPageResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimStatusHistoryResponse;
import com.talentfarm.sicms.claim.mapper.ClaimMapper;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.claim.repository.ClaimStatusHistoryRepository;
import com.talentfarm.sicms.customer.domain.Customer;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.policy.domain.Policy;
import com.talentfarm.sicms.policy.domain.PolicyStatus;
import com.talentfarm.sicms.policy.repository.PolicyRepository;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ClaimService {
    private static final int MAX_PAGE_SIZE = 50;
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final ClaimMapper mapper;
    private final ClaimStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ClaimService(CustomerRepository customerRepository, PolicyRepository policyRepository,
                        ClaimRepository claimRepository, ClaimMapper mapper, ClaimStatusHistoryRepository historyRepository,
                        UserRepository userRepository, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.mapper = mapper;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ClaimResponse submit(Authentication authentication, SubmitClaimRequest request) {
        Customer customer = currentCustomer(authentication);
        Policy policy = policyRepository.findByIdAndCustomerId(request.policyId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy was not found"));
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active policies are eligible for claims");
        }
        if (request.incidentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Incident date cannot be in the future");
        }
        if (request.incidentDate().isBefore(policy.getStartDate()) || request.incidentDate().isAfter(policy.getEndDate())) {
            throw new IllegalArgumentException("Incident date must fall within the policy period");
        }
        if (request.claimAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new IllegalArgumentException("Claim amount must not exceed policy coverage");
        }
        Claim claim = new Claim("CLM-" + UUID.randomUUID().toString().toUpperCase(), policy,
            request.claimAmount(), request.claimType(), request.incidentDate(), request.description().trim());
        Claim saved = claimRepository.saveAndFlush(claim);
        historyRepository.saveAndFlush(new com.talentfarm.sicms.claim.domain.ClaimStatusHistory(saved, null,
            com.talentfarm.sicms.claim.domain.ClaimStatus.SUBMITTED, currentUser(authentication), "initial submission"));
        auditService.record(AuditAction.CLAIM_SUBMITTED, currentUser(authentication), "CLAIM", saved.getId(), "Claim submitted");
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClaimPageResponse listMine(Authentication authentication, int page, int size) {
        validatePage(page, size);
        Page<Claim> claims = claimRepository.findByPolicyCustomerId(currentCustomer(authentication).getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")));
        return new ClaimPageResponse(claims.getContent().stream().map(mapper::toResponse).toList(), claims.getNumber(),
                claims.getSize(), claims.getTotalElements(), claims.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ClaimResponse getMine(Authentication authentication, UUID claimId) {
        Claim claim = claimRepository.findByIdAndPolicyCustomerId(claimId, currentCustomer(authentication).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim was not found"));
        return mapper.toResponse(claim);
    }

    @Transactional(readOnly = true)
    public java.util.List<ClaimStatusHistoryResponse> historyMine(Authentication authentication, UUID claimId) {
        Claim claim = claimRepository.findByIdAndPolicyCustomerId(claimId, currentCustomer(authentication).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim was not found"));
        return historyRepository.findByClaimIdOrderByChangedAtAscIdAsc(claim.getId()).stream().map(this::historyResponse).toList();
    }

    private Customer currentCustomer(Authentication authentication) {
        UUID userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found"));
    }

        private User currentUser(Authentication authentication) {
        return userRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
        }

        private ClaimStatusHistoryResponse historyResponse(com.talentfarm.sicms.claim.domain.ClaimStatusHistory history) {
        String actor = history.getChangedBy() == null ? "SYSTEM" : history.getChangedBy().getRoles().stream()
            .map(role -> role.getName().name()).findFirst().orElse("USER");
        return new ClaimStatusHistoryResponse(history.getId(), history.getFromStatus(), history.getToStatus(), actor,
            history.getChangeReason(), history.getChangedAt());
        }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 50");
        }
    }
}