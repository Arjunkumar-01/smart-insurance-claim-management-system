package com.talentfarm.sicms.administration.service;

import com.talentfarm.sicms.administration.dto.request.ChangeAccountStatusRequest;
import com.talentfarm.sicms.administration.dto.response.AdminUserPageResponse;
import com.talentfarm.sicms.administration.dto.response.AdminUserResponse;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.user.domain.AccountStatus;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdministrationService {
    private static final int MAX_PAGE_SIZE = 50;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AdministrationService(UserRepository userRepository, AuditService auditService) { this.userRepository = userRepository; this.auditService = auditService; }

    @Transactional(readOnly = true)
    public AdminUserPageResponse listUsers(int page, int size, String sort) {
        validatePage(page, size);
        String field = sort == null || sort.isBlank() ? "createdAt" : sort;
        if (!List.of("createdAt", "email", "lastName").contains(field)) {
            throw new IllegalArgumentException("Sort field is not supported");
        }
        Page<User> users = userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, field)));
        return new AdminUserPageResponse(users.getContent().stream().map(this::toResponse).toList(), users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(UUID userId) { return toResponse(findUser(userId)); }

    @Transactional
    public AdminUserResponse changeStatus(UUID targetId, UUID actorId, ChangeAccountStatusRequest request) {
        User target = findUser(targetId);
        if (target.getId().equals(actorId) && request.status() != AccountStatus.ACTIVE) {
            throw new DuplicateResourceException("An administrator cannot deactivate or lock their own account");
        }
        if (hasRole(target, ApplicationRole.ADMINISTRATOR) && target.getAccountStatus() == AccountStatus.ACTIVE
                && request.status() != AccountStatus.ACTIVE && activeAdministratorCount() <= 1) {
            throw new DuplicateResourceException("The last active Administrator cannot be deactivated or locked");
        }
        target.changeAccountStatus(request.status());
        auditService.record(AuditAction.ACCOUNT_STATUS_CHANGED, userRepository.findById(actorId).orElse(null), "USER", targetId, "Account status changed to " + request.status());
        return toResponse(target);
    }

    private User findUser(UUID id) { return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User was not found")); }
    private long activeAdministratorCount() { return userRepository.findAll().stream().filter(user -> user.getAccountStatus() == AccountStatus.ACTIVE && hasRole(user, ApplicationRole.ADMINISTRATOR)).count(); }
    private boolean hasRole(User user, ApplicationRole role) { return user.getRoles().stream().anyMatch(item -> item.getName() == role); }
    private AdminUserResponse toResponse(User user) { return new AdminUserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRoles().stream().map(role -> role.getName().name()).collect(java.util.stream.Collectors.toUnmodifiableSet()), user.getAccountStatus(), user.getCreatedAt(), user.getUpdatedAt()); }
    private void validatePage(int page, int size) { if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 50"); }
}