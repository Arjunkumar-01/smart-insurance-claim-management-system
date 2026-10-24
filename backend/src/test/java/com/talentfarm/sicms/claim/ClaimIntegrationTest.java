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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimIntegrationTest {
    private static final String PASSWORD = "Valid!Pass123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void customerSubmitsClaimWithServerStatusAndCanReadOwnClaim() throws Exception {
        String customer = customerWithProfile("claim.owner." + System.nanoTime() + "@example.test");
        UUID policyId = purchasePolicy(customer, activeProduct());
        String body = claimJson(policyId, "500.00", LocalDate.now().toString(), "Synthetic incident");

        String response = mockMvc.perform(post("/api/claims").header("Authorization", bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.claimNumber").value(containsString("CLM-")))
                .andExpect(jsonPath("$.coverageAmount").value(100000.00))
                .andExpect(jsonPath("$.claimType").value("HEALTH"))
                .andExpect(jsonPath("$.policyId").value(policyId.toString()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        JsonNode claim = objectMapper.readTree(response);
        String claimId = claim.get("claimId").asText();
        assertNotNull(claim.get("submittedAt"));

        mockMvc.perform(get("/api/claims/me").header("Authorization", bearer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.claims", hasSize(1)));
        mockMvc.perform(get("/api/claims/me/" + claimId).header("Authorization", bearer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void claimOwnershipEligibilityAndAmountRulesAreEnforced() throws Exception {
        String owner = customerWithProfile("claim.owner2." + System.nanoTime() + "@example.test");
        String other = customerWithProfile("claim.other." + System.nanoTime() + "@example.test");
        UUID policyId = purchasePolicy(owner, activeProduct());
        String validClaim = claimJson(policyId, "500.00", LocalDate.now().toString(), "Synthetic incident");

        mockMvc.perform(post("/api/claims").header("Authorization", bearer(other)).contentType(MediaType.APPLICATION_JSON).content(validClaim))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/claims").header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                        .content(claimJson(policyId, "100001.00", LocalDate.now().toString(), "Too large")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("coverage")));
        mockMvc.perform(post("/api/claims").header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                        .content(claimJson(policyId, "500.00", LocalDate.now().plusDays(1).toString(), "Future")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/claims").header("Authorization", bearer(owner)).contentType(MediaType.APPLICATION_JSON)
                        .content(claimJson(policyId, "500.00", "2020-01-01", "Outside period")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inactivePolicyAndNonCustomerRolesAreRejected() throws Exception {
        String customer = customerWithProfile("claim.inactive." + System.nanoTime() + "@example.test");
        Product product = productRepository.saveAndFlush(new Product("I" + (System.nanoTime() % 1000000000), "Inactive", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("1000.00"), new BigDecimal("10.00"), 12, ProductStatus.INACTIVE));
        mockMvc.perform(post("/api/claims").header("Authorization", bearer(customer)).contentType(MediaType.APPLICATION_JSON)
                        .content(claimJson(UUID.randomUUID(), "10.00", LocalDate.now().toString(), "Missing policy")))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/claims/me").header("Authorization", bearer(tokenForRole(ApplicationRole.AGENT))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/claims/me").header("Authorization", bearer(tokenForRole(ApplicationRole.CLAIMS_OFFICER))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/claims/me")).andExpect(status().isUnauthorized());
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }

    private Product activeProduct() {
        return productRepository.saveAndFlush(new Product("A" + (System.nanoTime() % 1000000000), "Active", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("100000.00"), new BigDecimal("125.50"), 12, ProductStatus.ACTIVE));
    }

    private UUID purchasePolicy(String token, Product product) throws Exception {
        String json = "{\"productId\":\"" + product.getId() + "\",\"startDate\":\"" + LocalDate.now() + "\"}";
        String response = mockMvc.perform(post("/api/policies").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("policyId").asText());
    }

    private String customerWithProfile(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Synthetic\",\"lastName\":\"Customer\",\"email\":\"" + email
                                + "\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+15550101100\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Synthetic Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        return token;
    }

    private String tokenForRole(ApplicationRole role) {
        ApplicationRoleEntity entity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".claim." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(entity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String claimJson(UUID policyId, String amount, String incidentDate, String description) {
        return "{\"policyId\":\"" + policyId + "\",\"claimAmount\":\"" + amount + "\",\"incidentDate\":\""
                + incidentDate + "\",\"claimType\":\"HEALTH\",\"description\":\"" + description + "\"}";
    }

    private String bearer(String token) { return "Bearer " + token; }
}