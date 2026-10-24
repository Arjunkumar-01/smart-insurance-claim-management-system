package com.talentfarm.sicms.claim.controller;

import com.talentfarm.sicms.claim.dto.request.SubmitClaimRequest;
import com.talentfarm.sicms.claim.dto.response.ClaimPageResponse;
import com.talentfarm.sicms.claim.dto.response.ClaimResponse;
import com.talentfarm.sicms.claim.service.ClaimService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@PreAuthorize("hasRole('CUSTOMER')")
public class ClaimController {
    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) { this.claimService = claimService; }

    @PostMapping
    public ResponseEntity<ClaimResponse> submit(Authentication authentication, @Valid @RequestBody SubmitClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.submit(authentication, request));
    }

    @GetMapping("/me")
    public ClaimPageResponse listMine(Authentication authentication, @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return claimService.listMine(authentication, page, size);
    }

    @GetMapping("/me/{claimId}")
    public ClaimResponse getMine(Authentication authentication, @PathVariable UUID claimId) {
        return claimService.getMine(authentication, claimId);
    }

    @GetMapping("/me/{claimId}/history")
    public java.util.List<com.talentfarm.sicms.claim.dto.response.ClaimStatusHistoryResponse> history(
            Authentication authentication, @PathVariable UUID claimId) {
        return claimService.historyMine(authentication, claimId);
    }
}