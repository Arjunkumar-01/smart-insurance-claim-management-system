package com.talentfarm.sicms.bootstrap;

import com.talentfarm.sicms.user.domain.AccountStatus;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@Profile("!test")
public class DevelopmentAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentAccountSeeder.class);

    private final DevelopmentBootstrapProperties properties;
    private final UserRepository userRepository;
    private final ApplicationRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentAccountSeeder(DevelopmentBootstrapProperties properties, UserRepository userRepository,
                                    ApplicationRoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            log.info("Development account bootstrap disabled.");
            return;
        }
        log.info("Development account bootstrap enabled.");
        seed(ApplicationRole.AGENT, properties.agent());
        seed(ApplicationRole.CLAIMS_OFFICER, properties.claimsOfficer());
        seed(ApplicationRole.ADMINISTRATOR, properties.administrator());
    }

    private void seed(ApplicationRole role, DevelopmentBootstrapProperties.Account account) {
        validate(account, role);
        String email = normalize(account.email());
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            verifyExisting(existing.get(), role, email);
            log.info("Development {} account available.", role);
            return;
        }

        ApplicationRoleEntity roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new IllegalStateException("Required application role is not configured: " + role));
        User user = new User(email, passwordEncoder.encode(account.password()), account.firstName().trim(), account.lastName().trim());
        user.addRole(roleEntity);
        userRepository.saveAndFlush(user);
        log.info("Development {} account created.", role);
    }

    private void verifyExisting(User user, ApplicationRole intendedRole, String email) {
        if (user.getAccountStatus() != AccountStatus.ACTIVE || user.getRoles().size() != 1
                || user.getRoles().stream().noneMatch(role -> role.getName() == intendedRole)) {
            throw new IllegalStateException("Development account has unexpected status or role: " + email);
        }
    }

    private void validate(DevelopmentBootstrapProperties.Account account, ApplicationRole role) {
        if (account == null || isBlank(account.email()) || isBlank(account.password())
                || isBlank(account.firstName()) || isBlank(account.lastName())) {
            throw new IllegalStateException("Development " + role + " account configuration is incomplete");
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}