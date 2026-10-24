package com.talentfarm.sicms.policy;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PolicyIntegrationTest {

    private static final String PASSWORD = "Valid!Pass123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void customerPurchasesWithProductSnapshotAndCanListOwnPolicy() throws Exception {
        String customerToken = customerWithProfile("policy.owner." + System.nanoTime() + "@example.test");
        Product product = activeProduct("P" + (System.nanoTime() % 1000000000), new BigDecimal("125.50"), new BigDecimal("100000.00"));
        String response = purchase(customerToken, product.getId(), true);

        mockMvc.perform(get("/api/policies/me").header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.policies", hasSize(1)))
                .andExpect(jsonPath("$.policies[0].premiumAmount").value(125.50))
                .andExpect(jsonPath("$.policies[0].coverageAmount").value(100000.00))
                .andExpect(jsonPath("$.policies[0].nominee.name").value("Synthetic Nominee"));

        JsonNode body = objectMapper.readTree(response);
        assertNotNull(body.get("policyNumber").asText());
        assertEquals(LocalDate.now().plusMonths(12).toString(), body.get("endDate").asText());
        assertEquals(125.50, productRepository.findById(product.getId()).orElseThrow().getPremiumAmount().doubleValue());
    }

    @Test
    void customerOwnershipRenewalNomineeAndCancellationWorkflowAreProtected() throws Exception {
        String customerAToken = customerWithProfile("policy.a." + System.nanoTime() + "@example.test");
        String customerBToken = customerWithProfile("policy.b." + System.nanoTime() + "@example.test");
        Product product = activeProduct("O" + (System.nanoTime() % 1000000000), new BigDecimal("200.00"), new BigDecimal("50000.00"));
        JsonNode policy = objectMapper.readTree(purchase(customerAToken, product.getId(), false));
        String policyId = policy.get("policyId").asText();
        String adminToken = tokenForRole(ApplicationRole.ADMINISTRATOR);

        mockMvc.perform(get("/api/policies/me/" + policyId).header("Authorization", bearer(customerBToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/policies/me/" + policyId + "/nominee").header("Authorization", bearer(customerBToken))
                        .contentType(MediaType.APPLICATION_JSON).content(nomineeJson("Intruder")))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/policies/me/" + policyId + "/renewal").header("Authorization", bearer(customerAToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endDate").value(LocalDate.now().plusMonths(24).toString()))
                .andExpect(jsonPath("$.premiumAmount").value(200.00));

        String cancellation = mockMvc.perform(post("/api/policies/me/" + policyId + "/cancellation")
                        .header("Authorization", bearer(customerAToken)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Synthetic cancellation request\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        String requestId = objectMapper.readTree(cancellation).get("requestId").asText();
        mockMvc.perform(post("/api/policies/me/" + policyId + "/cancellation").header("Authorization", bearer(customerAToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Duplicate\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/policies/cancellations/" + requestId + "/decision").header("Authorization", bearer(customerAToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/policies/cancellations/" + requestId + "/decision").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(get("/api/policies/me/" + policyId).header("Authorization", bearer(customerAToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(put("/api/policies/cancellations/" + requestId + "/decision").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void inactiveProductAndNonCustomerPolicyAccessAreRejected() throws Exception {
        String customerToken = customerWithProfile("policy.inactive." + System.nanoTime() + "@example.test");
        Product product = productRepository.saveAndFlush(new Product("I" + (System.nanoTime() % 1000000000), "Inactive", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("1000.00"), new BigDecimal("10.00"), 12, ProductStatus.INACTIVE));
        mockMvc.perform(post("/api/policies").header("Authorization", bearer(customerToken)).contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(product.getId(), false)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("active")));
        mockMvc.perform(get("/api/policies/me").header("Authorization", bearer(tokenForRole(ApplicationRole.AGENT))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/policies/me")).andExpect(status().isUnauthorized());
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
                        .content("{\"phoneNumber\":\"+15550101010\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Synthetic Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        return token;
    }

    private Product activeProduct(String code, BigDecimal premium, BigDecimal coverage) {
        return productRepository.saveAndFlush(new Product(code, "Synthetic Product", "Synthetic", ProductCategory.HEALTH,
                "Synthetic coverage", coverage, premium, 12, ProductStatus.ACTIVE));
    }

    private String purchase(String token, UUID productId, boolean nominee) throws Exception {
        return mockMvc.perform(post("/api/policies").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(productId, nominee))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    private String purchaseJson(UUID productId, boolean nominee) {
        return "{\"productId\":\"" + productId + "\",\"startDate\":\"" + LocalDate.now() + "\""
                + (nominee ? ",\"nominee\":{\"name\":\"Synthetic Nominee\",\"relationship\":\"Sibling\",\"contactNumber\":\"+15550101011\"}" : "") + "}";
    }

    private String nomineeJson(String name) { return "{\"name\":\"" + name + "\",\"relationship\":\"Sibling\",\"contactNumber\":\"+15550101012\"}"; }

    private String tokenForRole(ApplicationRole role) {
        ApplicationRoleEntity entity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".policy." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(entity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String bearer(String token) { return "Bearer " + token; }
}