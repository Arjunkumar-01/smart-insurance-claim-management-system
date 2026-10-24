package com.talentfarm.sicms.claim.controller;

import com.talentfarm.sicms.claim.dto.request.SettleClaimRequest;
import com.talentfarm.sicms.claim.dto.response.ClaimSettlementResponse;
import com.talentfarm.sicms.claim.service.ClaimSettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
public class ClaimSettlementController {
    private final ClaimSettlementService settlementService;

    public ClaimSettlementController(ClaimSettlementService settlementService) { this.settlementService = settlementService; }

    @PostMapping("/review/{claimId}/settle")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    public ResponseEntity<ClaimSettlementResponse> settle(Authentication authentication, @PathVariable UUID claimId,
                                                           @Valid @RequestBody SettleClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementService.settle(authentication, claimId, request));
    }

    @GetMapping("/me/{claimId}/settlement")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ClaimSettlementResponse get(Authentication authentication, @PathVariable UUID claimId) {
        return settlementService.getForCustomer(authentication, claimId);
    }
}