package com.talentfarm.sicms.security.service;

import com.talentfarm.sicms.user.domain.AccountStatus;
import com.talentfarm.sicms.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public final class CustomUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final AccountStatus accountStatus;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(UUID userId, String email, String passwordHash, AccountStatus accountStatus,
                              Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(User user) {
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .toList();
        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), user.getAccountStatus(), authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE;
    }
}