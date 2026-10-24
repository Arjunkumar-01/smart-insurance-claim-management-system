package com.talentfarm.sicms.customer.service;

import com.talentfarm.sicms.customer.domain.Customer;
import com.talentfarm.sicms.customer.domain.CustomerKyc;
import com.talentfarm.sicms.customer.dto.request.CreateCustomerProfileRequest;
import com.talentfarm.sicms.customer.dto.request.UpdateCustomerKycRequest;
import com.talentfarm.sicms.customer.dto.request.UpdateCustomerProfileRequest;
import com.talentfarm.sicms.customer.dto.response.CustomerKycResponse;
import com.talentfarm.sicms.customer.dto.response.CustomerProfileResponse;
import com.talentfarm.sicms.customer.mapper.CustomerMapper;
import com.talentfarm.sicms.customer.repository.CustomerKycRepository;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerKycRepository customerKycRepository;
    private final CustomerMapper mapper;

    public CustomerService(UserRepository userRepository, CustomerRepository customerRepository,
                           CustomerKycRepository customerKycRepository, CustomerMapper mapper) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.customerKycRepository = customerKycRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CustomerProfileResponse createProfile(Authentication authentication, CreateCustomerProfileRequest request) {
        User user = currentUser(authentication);
        if (customerRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Customer profile already exists");
        }
        Customer customer = new Customer(user, normalizePhone(request.phoneNumber()), request.dateOfBirth(), request.addressLine1(),
                request.addressLine2(), request.city(), request.state(), request.postalCode(), request.country());
        return mapper.toProfileResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Authentication authentication) {
        return mapper.toProfileResponse(currentCustomer(authentication));
    }

    @Transactional
    public CustomerProfileResponse updateProfile(Authentication authentication, UpdateCustomerProfileRequest request) {
        Customer customer = currentCustomer(authentication);
        customer.update(normalizePhone(request.phoneNumber()), request.dateOfBirth(), request.addressLine1(), request.addressLine2(),
                request.city(), request.state(), request.postalCode(), request.country());
        return mapper.toProfileResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerKycResponse getKyc(Authentication authentication) {
        Customer customer = currentCustomer(authentication);
        return mapper.toKycResponse(customerKycRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer KYC information was not found")));
    }

    @Transactional
    public CustomerKycResponse updateKyc(Authentication authentication, UpdateCustomerKycRequest request) {
        Customer customer = currentCustomer(authentication);
        CustomerKyc kyc = customerKycRepository.findByCustomerId(customer.getId()).orElse(null);
        if (kyc == null) {
            kyc = new CustomerKyc(customer, request.identityType().trim(), request.identityNumber().trim());
        } else {
            kyc.update(request.identityType().trim(), request.identityNumber().trim());
        }
        return mapper.toKycResponse(customerKycRepository.save(kyc));
    }

    private Customer currentCustomer(Authentication authentication) {
        return customerRepository.findByUserId(currentUserId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile was not found"));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findById(currentUserId(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private UUID currentUserId(Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
    }

    private String normalizePhone(String phoneNumber) {
        return phoneNumber.trim().replaceAll("[\\s().-]", "");
    }
}