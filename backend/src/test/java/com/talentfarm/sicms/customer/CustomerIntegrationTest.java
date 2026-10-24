package com.talentfarm.sicms.customer;

import com.fasterxml.jackson.databind.JsonNode;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerIntegrationTest {

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
    void customerCanCreateReadUpdateProfileAndKycWithoutLeakingIdentityNumber() throws Exception {
        String token = registerAndLogin("customer." + System.nanoTime() + "@example.test");

        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(profileJson("+1 (555) 010-1000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(containsString("@example.test")))
                .andExpect(jsonPath("$.phoneNumber").value("+15550101000"));

        mockMvc.perform(get("/api/customers/me/profile").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(put("/api/customers/me/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(profileJson("+1 555 010 2000")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.phoneNumber").value("+15550102000"));

        mockMvc.perform(put("/api/customers/me/kyc").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identityType\":\"national-id\",\"identityNumber\":\"SYNTHETIC-123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("PENDING"))
                .andExpect(jsonPath("$.maskedIdentityNumber").value("************3456"))
                .andExpect(jsonPath("$.identityNumber").doesNotExist());

        mockMvc.perform(get("/api/customers/me/kyc").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.maskedIdentityNumber").value("************3456"));
    }

    @Test
    void profileCreationIsSingleUseAndCustomerEndpointsRequireCustomerRole() throws Exception {
        String token = registerAndLogin("duplicate." + System.nanoTime() + "@example.test");
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(profileJson("+15550101000")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(profileJson("+15550101000")))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/customers/me/profile")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(agentToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(profileJson("+15550101000")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidProfileAndKycPayloadsAreRejected() throws Exception {
        String token = registerAndLogin("invalid." + System.nanoTime() + "@example.test");
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(profileJson("+15550101000").replace("2020-01-01", "2999-01-01")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/customers/me/kyc").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"identityType\":\"\",\"identityNumber\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void selfServiceIdentityCannotCrossCustomerBoundaries() throws Exception {
        String emailA = "customer-a." + System.nanoTime() + "@example.test";
        String emailB = "customer-b." + System.nanoTime() + "@example.test";
        String tokenA = registerAndLogin(emailA);
        String tokenB = registerAndLogin(emailB);
        createProfile(tokenA, "+15550101001");
        createProfile(tokenB, "+15550101002");

        mockMvc.perform(get("/api/customers/me/profile").header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(emailA));
        mockMvc.perform(get("/api/customers/me/profile").header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(emailB));
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Synthetic\",\"lastName\":\"Customer\",\"email\":\"" + email
                                + "\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.get("accessToken").asText();
    }

    private String agentToken() {
        ApplicationRoleEntity agentRole = roleRepository.findByName(ApplicationRole.AGENT).orElseThrow();
        User agent = new User("agent." + System.nanoTime() + "@example.test", passwordEncoder.encode(PASSWORD), "Synthetic", "Agent");
        agent.addRole(agentRole);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(agent))).value();
    }

        private void createProfile(String token, String phoneNumber) throws Exception {
                mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token))
                                                .contentType(MediaType.APPLICATION_JSON).content(profileJson(phoneNumber)))
                                .andExpect(status().isCreated());
        }

    private String profileJson(String phoneNumber) {
        return "{\"phoneNumber\":\"" + phoneNumber + "\",\"dateOfBirth\":\"2020-01-01\","
                + "\"addressLine1\":\"1 Example Street\",\"city\":\"Testville\",\"state\":\"Test State\","
                + "\"postalCode\":\"12345\",\"country\":\"Exampleland\"}";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}