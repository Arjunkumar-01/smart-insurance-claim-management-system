package com.talentfarm.sicms.administration.controller;

import com.talentfarm.sicms.administration.dto.request.ChangeAccountStatusRequest;
import com.talentfarm.sicms.administration.dto.response.AdminUserPageResponse;
import com.talentfarm.sicms.administration.dto.response.AdminUserResponse;
import com.talentfarm.sicms.administration.service.AdministrationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdministrationController {
    private final AdministrationService administrationService;

    public AdministrationController(AdministrationService administrationService) { this.administrationService = administrationService; }

    @GetMapping
    public AdminUserPageResponse listUsers(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(defaultValue = "createdAt") String sort) {
        return administrationService.listUsers(page, size, sort);
    }

    @GetMapping("/{userId}")
    public AdminUserResponse getUser(@PathVariable UUID userId) { return administrationService.getUser(userId); }

    @PatchMapping("/{userId}/status")
    public AdminUserResponse changeStatus(Authentication authentication, @PathVariable UUID userId,
                                          @Valid @RequestBody ChangeAccountStatusRequest request) {
        return administrationService.changeStatus(userId, ((com.talentfarm.sicms.security.service.CustomUserDetails) authentication.getPrincipal()).getUserId(), request);
    }
}