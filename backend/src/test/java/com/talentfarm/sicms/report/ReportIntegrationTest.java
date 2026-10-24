package com.talentfarm.sicms.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.ApplicationRole;
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

import java.time.LocalDate;
import java.time.Year;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportIntegrationTest {
    private static final String PASSWORD = "Valid!Pass123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void customerAndAdministratorReportsExposeRequiredClaimAndPolicyMeasures() throws Exception {
        String admin = tokenForRole(ApplicationRole.ADMINISTRATOR);
        String customer = customerWithProfile("report.customer." + System.nanoTime() + "@example.test");
        String product = mockMvc.perform(post("/api/products").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productCode\":\"RPT-" + (System.nanoTime() % 1000000000) + "\",\"name\":\"Report Product\",\"description\":\"Synthetic\",\"category\":\"HEALTH\",\"coverageDescription\":\"Coverage\",\"coverageAmount\":\"1000.00\",\"premiumAmount\":\"10.00\",\"tenureMonths\":12}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String productId = objectMapper.readTree(product).get("productId").asText();
        String policy = mockMvc.perform(post("/api/policies").header("Authorization", bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"startDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String policyId = objectMapper.readTree(policy).get("policyId").asText();
        mockMvc.perform(post("/api/claims").header("Authorization", bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + policyId + "\",\"claimAmount\":\"100.00\",\"claimType\":\"HEALTH\",\"incidentDate\":\"" + LocalDate.now() + "\",\"description\":\"Report claim\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/me/summary").header("Authorization", bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activePolicyCount").value(1))
                .andExpect(jsonPath("$.expiredPolicyCount").value(0))
                .andExpect(jsonPath("$.claimCount").value(1));

        mockMvc.perform(get("/api/reports/admin/overview").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClaims").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.pendingClaims").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.approvedClaims").isNumber())
                .andExpect(jsonPath("$.rejectedClaims").isNumber());

        mockMvc.perform(get("/api/reports/admin/settlements/trend").header("Authorization", bearer(admin))
                        .queryParam("year", String.valueOf(Year.now().getValue())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reports/admin/overview").header("Authorization", bearer(customer)))
                .andExpect(status().isForbidden());
    }

    private String customerWithProfile(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Synthetic\",\"lastName\":\"Customer\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+15550101600\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Report Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        return token;
    }

    private String tokenForRole(ApplicationRole role) {
        var roleEntity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".report." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(roleEntity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
