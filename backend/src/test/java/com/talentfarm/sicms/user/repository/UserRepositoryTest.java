package com.talentfarm.sicms.user.repository;

import com.talentfarm.sicms.user.domain.AccountStatus;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import com.talentfarm.sicms.user.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    private static final String PASSWORD_HASH = "test-only-password-hash";

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Autowired
    UserRepositoryTest(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Test
    void savesUserWithGeneratedUuidAndNormalizedEmail() {
        User user = userRepository.saveAndFlush(new User("  USER@EXAMPLE.TEST ", PASSWORD_HASH, "Test", "User"));

        assertNotNull(user.getId());
        assertEquals("user@example.test", user.getEmail());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(0L, user.getVersion());
    }

    @Test
    void findsAndChecksExistenceByEmailIgnoringCase() {
        userRepository.saveAndFlush(new User("user@example.test", PASSWORD_HASH, "Test", "User"));

        assertTrue(userRepository.existsByEmail("USER@EXAMPLE.TEST"));
        assertTrue(userRepository.findByEmail(" USER@example.test ").isPresent());
    }

    @Test
    void rejectsDuplicateEmailsIncludingCaseOnlyDifferences() {
        userRepository.saveAndFlush(new User("user@example.test", PASSWORD_HASH, "First", "User"));

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(new User("USER@EXAMPLE.TEST", PASSWORD_HASH, "Second", "User")));
    }

    @Test
    void persistsAccountStatusAndMultipleUniqueRoles() {
        ApplicationRoleEntity customer = role(ApplicationRole.CUSTOMER);
        ApplicationRoleEntity agent = role(ApplicationRole.AGENT);
        User user = new User("roles@example.test", PASSWORD_HASH, "Role", "User");
        user.changeAccountStatus(AccountStatus.INACTIVE);
        user.addRole(customer);
        user.addRole(customer);
        user.addRole(agent);

        User saved = userRepository.saveAndFlush(user);
        entityManager.clear();
        User reloaded = userRepository.findById(saved.getId()).orElseThrow();

        assertEquals(AccountStatus.INACTIVE, reloaded.getAccountStatus());
        assertEquals(2, reloaded.getRoles().size());
    }

    @Test
    void updatesTimestampAndOptimisticLockVersion() throws InterruptedException {
        User user = userRepository.saveAndFlush(new User("version@example.test", PASSWORD_HASH, "Version", "User"));
        Instant initialUpdatedAt = user.getUpdatedAt();
        Long initialVersion = user.getVersion();
        Thread.sleep(5);

        user.changeLastName("Updated");
        User updated = userRepository.saveAndFlush(user);

        assertTrue(updated.getUpdatedAt().isAfter(initialUpdatedAt));
        assertEquals(initialVersion + 1, updated.getVersion());
    }

    @Test
    void rejectsMissingRequiredFieldsAndDoesNotExposePasswordHash() {
        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(new User(null, PASSWORD_HASH, "Missing", "Email")));

        User user = new User("safe@example.test", PASSWORD_HASH, "Safe", "User");
        assertFalse(user.toString().contains(PASSWORD_HASH));
    }

    @Test
    void loadsAllFlywaySeededRoles() {
        for (ApplicationRole applicationRole : ApplicationRole.values()) {
            assertNotNull(role(applicationRole));
        }
    }

    private ApplicationRoleEntity role(ApplicationRole applicationRole) {
        return entityManager.createQuery(
                        "select role from ApplicationRoleEntity role where role.name = :name", ApplicationRoleEntity.class)
                .setParameter("name", applicationRole)
                .getSingleResult();
    }
}