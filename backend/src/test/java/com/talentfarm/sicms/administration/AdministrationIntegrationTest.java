package com.talentfarm.sicms.administration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministrationIntegrationTest {
    private static final String PASSWORD = "Valid!Pass123";
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void administratorCanListDetailAndChangeSyntheticAccountStatus() throws Exception {
        String adminToken = tokenForRole(ApplicationRole.ADMINISTRATOR);
        User target = userRepository.saveAndFlush(new User("admin.target." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", "Target"));
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(adminToken)).queryParam("page", "0").queryParam("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.users").isArray()).andExpect(jsonPath("$.users.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.users[0].passwordHash").doesNotExist());
        mockMvc.perform(get("/api/admin/users/" + target.getId()).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.passwordHash").doesNotExist());
        mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/status").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accountStatus").value("INACTIVE"));
    }

    @Test
    void nonAdministratorsCannotUseAdministrationAndLastAdminProtectionApplies() throws Exception {
        String customerToken = tokenForRole(ApplicationRole.CUSTOMER);
                TestAdmin admin = adminToken();
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(customerToken))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/admin/users/" + admin.user().getId() + "/status").header("Authorization", bearer(admin.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"LOCKED\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("cannot deactivate")));
    }

    private String tokenForRole(ApplicationRole role) {
        ApplicationRoleEntity entity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".admin." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(entity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

        private TestAdmin adminToken() {
                ApplicationRoleEntity entity = roleRepository.findByName(ApplicationRole.ADMINISTRATOR).orElseThrow();
                User user = new User("administrator.admin." + System.nanoTime() + "@example.test",
                                passwordEncoder.encode(PASSWORD), "Synthetic", "Administrator");
                user.addRole(entity);
                User saved = userRepository.saveAndFlush(user);
                return new TestAdmin(saved, jwtTokenProvider.generateAccessToken(CustomUserDetails.from(saved)).value());
        }

        private record TestAdmin(User user, String token) { }

    private String bearer(String token) { return "Bearer " + token; }
}
