package com.talentfarm.sicms.claim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.domain.ProductCategory;
import com.talentfarm.sicms.product.domain.ProductStatus;
import com.talentfarm.sicms.product.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimSettlementIntegrationTest {
    private static final String PASSWORD = "Valid!Pass123";
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void claimsOfficerSettlesApprovedClaimAndCustomerCanSeeSettlement() throws Exception {
        TestClaim setup = customerClaim("settlement.customer." + System.nanoTime() + "@example.test");
        String officer = roleToken(ApplicationRole.CLAIMS_OFFICER);
        startAndApprove(setup.claimId(), officer);

        String settlement = mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/settle")
                        .header("Authorization", bearer(officer)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Synthetic settlement\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.settlementNumber").value(containsString("STL-")))
                .andExpect(jsonPath("$.settlementAmount").value(25.00))
                .andExpect(jsonPath("$.claimId").value(setup.claimId()))
                .andReturn().getResponse().getContentAsString();
        String settlementId = objectMapper.readTree(settlement).get("settlementId").asText();

        mockMvc.perform(get("/api/claims/me/" + setup.claimId()).header("Authorization", bearer(setup.customerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SETTLED"));
        mockMvc.perform(get("/api/claims/me/" + setup.claimId() + "/settlement").header("Authorization", bearer(setup.customerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.settlementId").value(settlementId));
        mockMvc.perform(get("/api/claims/me/" + setup.claimId() + "/history").header("Authorization", bearer(setup.customerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("[-1].toStatus").value("SETTLED"));
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/settle").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void settlementRequiresApprovedClaimAndClaimsOfficerRole() throws Exception {
        TestClaim setup = customerClaim("settlement.invalid." + System.nanoTime() + "@example.test");
        String officer = roleToken(ApplicationRole.CLAIMS_OFFICER);
        String agent = roleToken(ApplicationRole.AGENT);
        String admin = roleToken(ApplicationRole.ADMINISTRATOR);
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/settle").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("approved")));
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/settle").header("Authorization", bearer(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/settle").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/settle").header("Authorization", bearer(setup.customerToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    private void startAndApprove(String claimId, String officer) throws Exception {
        mockMvc.perform(post("/api/claims/review/" + claimId + "/start").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/claims/review/" + claimId + "/approve").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    private TestClaim customerClaim(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Synthetic\",\"lastName\":\"Customer\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+15550101500\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Settlement Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        Product product = productRepository.saveAndFlush(new Product("S" + (System.nanoTime() % 1000000000), "Settlement Product", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("100000.00"), new BigDecimal("10.00"), 12, ProductStatus.ACTIVE));
        String policy = mockMvc.perform(post("/api/policies").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + product.getId() + "\",\"startDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String claim = mockMvc.perform(post("/api/claims").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + objectMapper.readTree(policy).get("policyId").asText() + "\",\"claimAmount\":\"25.00\",\"claimType\":\"HEALTH\",\"incidentDate\":\"" + LocalDate.now() + "\",\"description\":\"Settlement claim\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new TestClaim(token, objectMapper.readTree(claim).get("claimId").asText());
    }

    private String roleToken(ApplicationRole role) {
        ApplicationRoleEntity entity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".settlement." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(entity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String bearer(String token) { return "Bearer " + token; }
    private record TestClaim(String customerToken, String claimId) { }
}
