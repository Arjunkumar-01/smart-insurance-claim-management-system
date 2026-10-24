package com.talentfarm.sicms.policy.mapper;

import com.talentfarm.sicms.policy.domain.Policy;
import com.talentfarm.sicms.policy.domain.PolicyCancellationRequest;
import com.talentfarm.sicms.policy.domain.PolicyNominee;
import com.talentfarm.sicms.policy.dto.response.PolicyCancellationResponse;
import com.talentfarm.sicms.policy.dto.response.PolicyNomineeResponse;
import com.talentfarm.sicms.policy.dto.response.PolicyResponse;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {
    public PolicyResponse toResponse(Policy policy, PolicyNominee nominee) {
        var product = policy.getProduct();
        return new PolicyResponse(policy.getId(), policy.getPolicyNumber(), product.getId(), product.getProductCode(),
            product.getName(), product.getTenureMonths(), policy.getPremiumAmount(), policy.getCoverageAmount(), policy.getStartDate(),
                policy.getEndDate(), policy.getStatus(), nominee == null ? null : toNomineeResponse(nominee),
                policy.getCreatedAt(), policy.getUpdatedAt());
    }

    public PolicyNomineeResponse toNomineeResponse(PolicyNominee nominee) {
        return new PolicyNomineeResponse(nominee.getId(), nominee.getName(), nominee.getRelationship(), nominee.getContactNumber());
    }

    public PolicyCancellationResponse toCancellationResponse(PolicyCancellationRequest request) {
        return new PolicyCancellationResponse(request.getId(), request.getPolicy().getId(), request.getPolicy().getPolicyNumber(),
                request.getStatus(), request.getReason(), request.getDecisionComment(), request.getRequestedAt(), request.getDecidedAt());
    }
}