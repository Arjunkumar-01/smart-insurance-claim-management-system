package com.talentfarm.sicms.policy.dto.response;

import java.util.UUID;

public record PolicyNomineeResponse(UUID nomineeId, String name, String relationship, String contactNumber) { }