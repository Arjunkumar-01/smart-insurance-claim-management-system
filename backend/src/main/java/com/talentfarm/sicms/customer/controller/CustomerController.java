package com.talentfarm.sicms.customer.controller;

import com.talentfarm.sicms.customer.dto.request.CreateCustomerProfileRequest;
import com.talentfarm.sicms.customer.dto.request.UpdateCustomerKycRequest;
import com.talentfarm.sicms.customer.dto.request.UpdateCustomerProfileRequest;
import com.talentfarm.sicms.customer.dto.response.CustomerKycResponse;
import com.talentfarm.sicms.customer.dto.response.CustomerProfileResponse;
import com.talentfarm.sicms.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/profile")
    public ResponseEntity<CustomerProfileResponse> createProfile(Authentication authentication,
                                                                  @Valid @RequestBody CreateCustomerProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createProfile(authentication, request));
    }

    @GetMapping("/profile")
    public CustomerProfileResponse getProfile(Authentication authentication) {
        return customerService.getProfile(authentication);
    }

    @PutMapping("/profile")
    public CustomerProfileResponse updateProfile(Authentication authentication,
                                                 @Valid @RequestBody UpdateCustomerProfileRequest request) {
        return customerService.updateProfile(authentication, request);
    }

    @GetMapping("/kyc")
    public CustomerKycResponse getKyc(Authentication authentication) {
        return customerService.getKyc(authentication);
    }

    @PutMapping("/kyc")
    public CustomerKycResponse updateKyc(Authentication authentication,
                                         @Valid @RequestBody UpdateCustomerKycRequest request) {
        return customerService.updateKyc(authentication, request);
    }
}