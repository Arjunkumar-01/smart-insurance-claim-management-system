package com.talentfarm.sicms.customer.mapper;

import com.talentfarm.sicms.customer.domain.Customer;
import com.talentfarm.sicms.customer.domain.CustomerKyc;
import com.talentfarm.sicms.customer.dto.response.CustomerKycResponse;
import com.talentfarm.sicms.customer.dto.response.CustomerProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerProfileResponse toProfileResponse(Customer customer) {
        var user = customer.getUser();
        return new CustomerProfileResponse(customer.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                customer.getPhoneNumber(), customer.getDateOfBirth(), customer.getAddressLine1(), customer.getAddressLine2(),
                customer.getCity(), customer.getState(), customer.getPostalCode(), customer.getCountry(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }

    public CustomerKycResponse toKycResponse(CustomerKyc kyc) {
        return new CustomerKycResponse(kyc.getId(), kyc.getIdentityType(), mask(kyc.getIdentityNumber()),
                kyc.getKycStatus(), kyc.getCreatedAt(), kyc.getUpdatedAt());
    }

    private String mask(String value) {
        if (value.length() <= 4) {
            return "****";
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}