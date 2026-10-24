package com.talentfarm.sicms.domain;

import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ControlledEnumsTest {

    @Test
    void claimStatusesMatchTheDefinedWorkflow() {
        assertArrayEquals(new ClaimStatus[]{
                ClaimStatus.SUBMITTED,
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.DOCUMENTS_REQUIRED,
                ClaimStatus.APPROVED,
                ClaimStatus.REJECTED,
                ClaimStatus.SETTLED
        }, ClaimStatus.values());
    }

    @Test
    void applicationRolesMatchTheAuthorizationContract() {
        assertArrayEquals(new ApplicationRole[]{
                ApplicationRole.CUSTOMER,
                ApplicationRole.AGENT,
                ApplicationRole.CLAIMS_OFFICER,
                ApplicationRole.ADMINISTRATOR
        }, ApplicationRole.values());
    }
}