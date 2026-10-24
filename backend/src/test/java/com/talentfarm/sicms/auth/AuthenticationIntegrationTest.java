package com.talentfarm.sicms.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.user.domain.AccountStatus;
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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    private static final String PASSWORD = "Valid!Pass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void registersCustomerWithNormalizedEmailAndBcryptHash() throws Exception {
        String email = "  Customer." + System.nanoTime() + "@EXAMPLE.TEST ";
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email.trim().toLowerCase()))
                .andExpect(jsonPath("$.user.roles", hasSize(1)))
                .andExpect(jsonPath("$.user.roles[0]").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.accountStatus").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        assertFalse(response.contains("password"));
        User user = userRepository.findByEmail(email).orElseThrow();
        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPasswordHash()));
        assertEquals(1, user.getRoles().size());
        assertEquals(ApplicationRole.CUSTOMER, user.getRoles().iterator().next().getName());
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        String email = "duplicate." + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerJson(email, PASSWORD)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerJson(email.toUpperCase(), PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void loginReturnsExpiringBearerTokenAndInvalidPasswordIsUnauthorized() throws Exception {
        String email = "login." + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerJson(email, PASSWORD)))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\" " + email.toUpperCase() + " \",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.expiresAt").isString())
                .andReturn().getResponse().getContentAsString();
        JsonNode token = objectMapper.readTree(response).get("accessToken");
        assertNotNull(token);
        assertEquals(email, jwtTokenProvider.parseAndValidate(token.asText()).getSubject());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Wrong!Pass123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void inactiveAndLockedAccountsCannotLogin() throws Exception {
        String email = "status." + System.nanoTime() + "@example.test";
        ApplicationRoleEntity customer = roleRepository.findByName(ApplicationRole.CUSTOMER).orElseThrow();
        User user = new User(email, passwordEncoder.encode(PASSWORD), "Status", "User");
        user.addRole(customer);
        user.changeAccountStatus(AccountStatus.INACTIVE);
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson(email)))
                .andExpect(status().isUnauthorized());

        user.changeAccountStatus(AccountStatus.LOCKED);
        userRepository.saveAndFlush(user);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedResourcesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected-placeholder"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRegistrationIsRejectedWithoutCreatingAnAccount() throws Exception {
        String email = "invalid." + System.nanoTime() + "@example.test";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email, "weak" ).replace("\"confirmPassword\":\"weak\"", "\"confirmPassword\":\"different\"")))
                .andExpect(status().isBadRequest());
        assertTrue(userRepository.findByEmail(email).isEmpty());
    }

    private String registerJson(String email, String password) {
        return "{\"firstName\":\"Test\",\"lastName\":\"Customer\",\"email\":\"" + email +
                "\",\"password\":\"" + password + "\",\"confirmPassword\":\"" + password + "\"}";
    }

    private String loginJson(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}";
    }
}