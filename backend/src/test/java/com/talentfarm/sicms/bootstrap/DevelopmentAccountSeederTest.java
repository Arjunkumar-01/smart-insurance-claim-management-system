package com.talentfarm.sicms.bootstrap;

import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentAccountSeederTest {

    private static final String HASH = "$2a$10$development-only-test-hash";

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsEachConfiguredPrivilegedAccountWithEncodedPasswordAndExactRole() {
        ApplicationRoleEntity agent = role(ApplicationRole.AGENT);
        ApplicationRoleEntity claims = role(ApplicationRole.CLAIMS_OFFICER);
        ApplicationRoleEntity administrator = role(ApplicationRole.ADMINISTRATOR);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(roleRepository.findByName(ApplicationRole.AGENT)).thenReturn(Optional.of(agent));
        when(roleRepository.findByName(ApplicationRole.CLAIMS_OFFICER)).thenReturn(Optional.of(claims));
        when(roleRepository.findByName(ApplicationRole.ADMINISTRATOR)).thenReturn(Optional.of(administrator));
        when(passwordEncoder.encode(any())).thenReturn(HASH);

        newSeeder(true).run(new DefaultApplicationArguments());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, org.mockito.Mockito.times(3)).saveAndFlush(captor.capture());
        assertEquals(3, captor.getAllValues().size());
        assertEquals(HASH, captor.getAllValues().get(0).getPasswordHash());
        assertEquals(1, captor.getAllValues().get(0).getRoles().size());
        verify(passwordEncoder, org.mockito.Mockito.times(3)).encode(any());
    }

    @Test
    void existingCorrectAccountsAreRetainedWithoutResettingPasswordOrDuplicatingRoles() {
        ApplicationRoleEntity agent = role(ApplicationRole.AGENT);
        User existing = new User("agent.local@example.test", "existing-hash", "Local", "Agent");
        existing.addRole(agent);
        when(userRepository.findByEmail("agent.local@example.test")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("claims.officer.local@example.test")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("administrator.local@example.test")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ApplicationRole.CLAIMS_OFFICER)).thenReturn(Optional.of(role(ApplicationRole.CLAIMS_OFFICER)));
        when(roleRepository.findByName(ApplicationRole.ADMINISTRATOR)).thenReturn(Optional.of(role(ApplicationRole.ADMINISTRATOR)));
        when(passwordEncoder.encode(any())).thenReturn(HASH);

        newSeeder(true).run(new DefaultApplicationArguments());

        assertEquals("existing-hash", existing.getPasswordHash());
        assertEquals(1, existing.getRoles().size());
        verify(passwordEncoder, org.mockito.Mockito.times(2)).encode(any());
        verify(userRepository, org.mockito.Mockito.times(2)).saveAndFlush(any(User.class));
    }

    @Test
    void disabledBootstrapDoesNothing() {
        newSeeder(false).run(new DefaultApplicationArguments());

        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void missingConfiguredRoleFailsClearly() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(roleRepository.findByName(ApplicationRole.AGENT)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> newSeeder(true).run(new DefaultApplicationArguments()));
    }

    private DevelopmentAccountSeeder newSeeder(boolean enabled) {
        return new DevelopmentAccountSeeder(properties(enabled), userRepository, roleRepository, passwordEncoder);
    }

    private DevelopmentBootstrapProperties properties(boolean enabled) {
        return new DevelopmentBootstrapProperties(enabled,
                new DevelopmentBootstrapProperties.Account("agent.local@example.test", "Agent!123", "Local", "Agent"),
                new DevelopmentBootstrapProperties.Account("claims.officer.local@example.test", "Claims!123", "Local", "ClaimsOfficer"),
                new DevelopmentBootstrapProperties.Account("administrator.local@example.test", "Admin!123", "Local", "Administrator"));
    }

    private ApplicationRoleEntity role(ApplicationRole role) {
        return new ApplicationRoleEntity(UUID.randomUUID(), role);
    }
}