package com.talentfarm.sicms.administration.dto.response;

import java.util.List;

public record AdminUserPageResponse(List<AdminUserResponse> users, int page, int size, long totalElements, int totalPages) { }