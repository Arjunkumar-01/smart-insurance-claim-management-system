package com.talentfarm.sicms.auth.service;

import com.talentfarm.sicms.auth.dto.request.LoginRequest;
import com.talentfarm.sicms.auth.dto.request.RegisterRequest;
import com.talentfarm.sicms.auth.dto.response.LoginResponse;
import com.talentfarm.sicms.auth.dto.response.RegisterResponse;
import com.talentfarm.sicms.auth.dto.response.UserSummary;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.exception.InvalidConfigurationException;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ApplicationRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, ApplicationRoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        var customerRole = roleRepository.findByName(ApplicationRole.CUSTOMER)
                .orElseThrow(() -> new InvalidConfigurationException("Customer role is not configured"));
        User user = new User(normalizedEmail, passwordEncoder.encode(request.password()), request.firstName().trim(), request.lastName().trim());
        user.addRole(customerRole);
        try {
            User saved = userRepository.saveAndFlush(user);
            auditService.record(AuditAction.USER_REGISTERED, saved, "USER", saved.getId(), "User registered");
            return new RegisterResponse(toSummary(saved));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        var token = jwtTokenProvider.generateAccessToken(userDetails);
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        return new LoginResponse(token.value(), "Bearer", token.expiresAt(), toSummary(user));
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                user.getAccountStatus(), user.getCreatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}