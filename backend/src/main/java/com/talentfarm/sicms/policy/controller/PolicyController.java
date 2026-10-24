package com.talentfarm.sicms.policy.controller;

import com.talentfarm.sicms.policy.dto.request.DecidePolicyCancellationRequest;
import com.talentfarm.sicms.policy.dto.request.PurchasePolicyRequest;
import com.talentfarm.sicms.policy.dto.request.RequestPolicyCancellationRequest;
import com.talentfarm.sicms.policy.dto.request.RenewPolicyRequest;
import com.talentfarm.sicms.policy.dto.request.UpdateNomineeRequest;
import com.talentfarm.sicms.policy.dto.response.PolicyCancellationResponse;
import com.talentfarm.sicms.policy.dto.response.PolicyPageResponse;
import com.talentfarm.sicms.policy.dto.response.PolicyResponse;
import com.talentfarm.sicms.policy.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) { this.policyService = policyService; }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyResponse> purchase(Authentication authentication, @Valid @RequestBody PurchasePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.purchase(authentication, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PolicyPageResponse listMine(Authentication authentication, @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return policyService.listMine(authentication, page, size);
    }

    @GetMapping("/me/{policyId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PolicyResponse getMine(Authentication authentication, @PathVariable UUID policyId) {
        return policyService.getMine(authentication, policyId);
    }

    @PutMapping("/me/{policyId}/renewal")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PolicyResponse renew(Authentication authentication, @PathVariable UUID policyId,
                                @Valid @RequestBody RenewPolicyRequest request) {
        return policyService.renew(authentication, policyId, request);
    }

    @PutMapping("/me/{policyId}/nominee")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PolicyResponse updateNominee(Authentication authentication, @PathVariable UUID policyId,
                                        @Valid @RequestBody UpdateNomineeRequest request) {
        return policyService.updateNominee(authentication, policyId, request);
    }

    @PostMapping("/me/{policyId}/cancellation")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyCancellationResponse> requestCancellation(Authentication authentication,
                                                                            @PathVariable UUID policyId,
                                                                            @Valid @RequestBody RequestPolicyCancellationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.requestCancellation(authentication, policyId, request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PolicyPageResponse listAll(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return policyService.listAll(page, size);
    }

    @GetMapping("/cancellations")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<PolicyCancellationResponse> pendingCancellations() {
        return policyService.pendingCancellations();
    }

    @PutMapping("/cancellations/{requestId}/decision")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PolicyCancellationResponse decideCancellation(@PathVariable UUID requestId,
                                                          @Valid @RequestBody DecidePolicyCancellationRequest request) {
        return policyService.decideCancellation(requestId, request);
    }
}