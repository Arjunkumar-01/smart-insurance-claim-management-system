package com.talentfarm.sicms.claim.service;

import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.claim.domain.ClaimSettlement;
import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.claim.dto.request.SettleClaimRequest;
import com.talentfarm.sicms.claim.dto.response.ClaimSettlementResponse;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.claim.repository.ClaimSettlementRepository;
import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClaimSettlementService {
    private final ClaimRepository claimRepository;
    private final ClaimSettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final ClaimStatusTransitionService transitionService;
    private final AuditService auditService;

    public ClaimSettlementService(ClaimRepository claimRepository, ClaimSettlementRepository settlementRepository,
                                  UserRepository userRepository, ClaimStatusTransitionService transitionService, AuditService auditService) {
        this.claimRepository = claimRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
        this.transitionService = transitionService;
        this.auditService = auditService;
    }

    @Transactional
    public ClaimSettlementResponse settle(Authentication authentication, UUID claimId, SettleClaimRequest request) {
        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new ResourceNotFoundException("Claim was not found"));
        if (settlementRepository.existsByClaimId(claimId)) {
            throw new DuplicateResourceException("Claim has already been settled");
        }
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved claims can be settled");
        }
        User actor = userRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Settlement actor was not found"));
        ClaimSettlement settlement = new ClaimSettlement("STL-" + UUID.randomUUID().toString().toUpperCase(), claim,
                claim.getClaimAmount(), actor, request == null || request.notes() == null ? null : request.notes().trim());
        try {
            ClaimSettlement saved = settlementRepository.saveAndFlush(settlement);
            transitionService.transition(claim, ClaimStatus.SETTLED, actor, "claim settled");
            auditService.record(AuditAction.CLAIM_SETTLED, actor, "CLAIM", claimId, "Claim settled");
            return response(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Claim has already been settled");
        }
    }

    @Transactional(readOnly = true)
    public ClaimSettlementResponse getForCustomer(Authentication authentication, UUID claimId) {
        UUID userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        ClaimSettlement settlement = settlementRepository.findByClaimId(claimId)
                .filter(item -> item.getClaim().getPolicy().getCustomer().getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Settlement was not found"));
        return response(settlement);
    }

    private ClaimSettlementResponse response(ClaimSettlement settlement) {
        return new ClaimSettlementResponse(settlement.getId(), settlement.getSettlementNumber(), settlement.getClaim().getId(),
                settlement.getClaim().getClaimNumber(), settlement.getSettlementAmount(), settlement.getSettledAt());
    }
}