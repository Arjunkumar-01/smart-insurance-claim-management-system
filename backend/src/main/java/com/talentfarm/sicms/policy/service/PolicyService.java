package com.talentfarm.sicms.policy.service;

import com.talentfarm.sicms.customer.domain.Customer;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.policy.domain.CancellationStatus;
import com.talentfarm.sicms.policy.domain.Policy;
import com.talentfarm.sicms.policy.domain.PolicyCancellationRequest;
import com.talentfarm.sicms.policy.domain.PolicyNominee;
import com.talentfarm.sicms.policy.domain.PolicyStatus;
import com.talentfarm.sicms.policy.dto.request.DecidePolicyCancellationRequest;
import com.talentfarm.sicms.policy.dto.request.PurchasePolicyRequest;
import com.talentfarm.sicms.policy.dto.request.RequestPolicyCancellationRequest;
import com.talentfarm.sicms.policy.dto.request.RenewPolicyRequest;
import com.talentfarm.sicms.policy.dto.request.UpdateNomineeRequest;
import com.talentfarm.sicms.policy.dto.response.PolicyCancellationResponse;
import com.talentfarm.sicms.policy.dto.response.PolicyPageResponse;
import com.talentfarm.sicms.policy.dto.response.PolicyResponse;
import com.talentfarm.sicms.policy.mapper.PolicyMapper;
import com.talentfarm.sicms.policy.repository.PolicyCancellationRequestRepository;
import com.talentfarm.sicms.policy.repository.PolicyNomineeRepository;
import com.talentfarm.sicms.policy.repository.PolicyRepository;
import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.domain.ProductStatus;
import com.talentfarm.sicms.product.repository.ProductRepository;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PolicyService {
    private static final int MAX_PAGE_SIZE = 50;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final PolicyNomineeRepository nomineeRepository;
    private final PolicyCancellationRequestRepository cancellationRepository;
    private final PolicyMapper mapper;

    public PolicyService(CustomerRepository customerRepository, ProductRepository productRepository, PolicyRepository policyRepository,
                         PolicyNomineeRepository nomineeRepository, PolicyCancellationRequestRepository cancellationRepository,
                         PolicyMapper mapper) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.nomineeRepository = nomineeRepository;
        this.cancellationRepository = cancellationRepository;
        this.mapper = mapper;
    }

    @Transactional
    public PolicyResponse purchase(Authentication authentication, PurchasePolicyRequest request) {
        Customer customer = currentCustomer(authentication);
        Product product = productRepository.findById(request.productId()).orElseThrow(() -> new ResourceNotFoundException("Product was not found"));
        if (product.getStatus() != ProductStatus.ACTIVE) throw new IllegalArgumentException("Only active products can be purchased");
        LocalDate endDate = request.startDate().plusMonths(product.getTenureMonths());
        Policy policy = new Policy("POL-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT), customer, product,
            product.getPremiumAmount(), product.getCoverageAmount(), request.startDate(), endDate);
        policyRepository.saveAndFlush(policy);
        PolicyNominee nominee = request.nominee() == null ? null : nomineeRepository.save(new PolicyNominee(policy,
                request.nominee().name().trim(), request.nominee().relationship().trim(), clean(request.nominee().contactNumber())));
        return mapper.toResponse(policy, nominee);
    }

    @Transactional(readOnly = true)
    public PolicyPageResponse listMine(Authentication authentication, int page, int size) {
        validatePage(page, size);
        Page<Policy> result = policyRepository.findByCustomerId(currentCustomer(authentication).getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return toPage(result);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getMine(Authentication authentication, UUID policyId) {
        Policy policy = policyRepository.findByIdAndCustomerId(policyId, currentCustomer(authentication).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy was not found"));
        return mapper.toResponse(policy, nomineeRepository.findByPolicyId(policyId).orElse(null));
    }

    @Transactional
    public PolicyResponse renew(Authentication authentication, UUID policyId, RenewPolicyRequest request) {
        Policy policy = ownedPolicy(authentication, policyId);
        if (policy.getStatus() != PolicyStatus.ACTIVE) throw new IllegalArgumentException("Only active policies can be renewed");
        policy.renew(policy.getEndDate().plusMonths(policy.getProduct().getTenureMonths()));
        return mapper.toResponse(policy, nomineeRepository.findByPolicyId(policyId).orElse(null));
    }

    @Transactional
    public PolicyResponse updateNominee(Authentication authentication, UUID policyId, UpdateNomineeRequest request) {
        Policy policy = ownedPolicy(authentication, policyId);
        PolicyNominee nominee = nomineeRepository.findByPolicyId(policyId).orElse(null);
        if (nominee == null) nominee = new PolicyNominee(policy, request.name().trim(), request.relationship().trim(), clean(request.contactNumber()));
        else nominee.update(request.name().trim(), request.relationship().trim(), clean(request.contactNumber()));
        return mapper.toResponse(policy, nomineeRepository.save(nominee));
    }

    @Transactional
    public PolicyCancellationResponse requestCancellation(Authentication authentication, UUID policyId, RequestPolicyCancellationRequest request) {
        Policy policy = ownedPolicy(authentication, policyId);
        if (policy.getStatus() != PolicyStatus.ACTIVE) throw new IllegalArgumentException("Only active policies can be cancelled");
        if (cancellationRepository.existsByPolicyIdAndStatus(policyId, CancellationStatus.PENDING))
            throw new DuplicateResourceException("A pending cancellation request already exists");
        return mapper.toCancellationResponse(cancellationRepository.save(new PolicyCancellationRequest(policy, clean(request.reason()))));
    }

    @Transactional(readOnly = true)
    public List<PolicyCancellationResponse> pendingCancellations() {
        return cancellationRepository.findByStatusOrderByRequestedAtAsc(CancellationStatus.PENDING).stream().map(mapper::toCancellationResponse).toList();
    }

    @Transactional
    public PolicyCancellationResponse decideCancellation(UUID requestId, DecidePolicyCancellationRequest request) {
        PolicyCancellationRequest cancellation = cancellationRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request was not found"));
        if (cancellation.getStatus() != CancellationStatus.PENDING) throw new DuplicateResourceException("Cancellation request was already decided");
        CancellationStatus decision = cancellationStatus(request.status());
        cancellation.decide(decision, clean(request.decisionComment()));
        if (decision == CancellationStatus.APPROVED) cancellation.getPolicy().cancel();
        return mapper.toCancellationResponse(cancellation);
    }

    @Transactional(readOnly = true)
    public PolicyPageResponse listAll(int page, int size) {
        validatePage(page, size);
        return toPage(policyRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    private Customer currentCustomer(Authentication authentication) {
        UUID userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        return customerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found"));
    }

    private Policy ownedPolicy(Authentication authentication, UUID policyId) {
        return policyRepository.findByIdAndCustomerId(policyId, currentCustomer(authentication).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy was not found"));
    }

    private PolicyPageResponse toPage(Page<Policy> page) {
        List<PolicyResponse> policies = page.getContent().stream().map(policy -> mapper.toResponse(policy, nomineeRepository.findByPolicyId(policy.getId()).orElse(null))).toList();
        return new PolicyPageResponse(policies, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 50");
    }

    private CancellationStatus cancellationStatus(String value) {
        try { return CancellationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("Cancellation status must be APPROVED or REJECTED"); }
    }

    private String clean(String value) { return value == null ? null : value.trim(); }
}