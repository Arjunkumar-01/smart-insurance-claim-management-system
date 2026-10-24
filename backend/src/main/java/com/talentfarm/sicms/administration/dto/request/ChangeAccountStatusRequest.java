package com.talentfarm.sicms.administration.dto.request;

import com.talentfarm.sicms.user.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeAccountStatusRequest(@NotNull AccountStatus status) { }