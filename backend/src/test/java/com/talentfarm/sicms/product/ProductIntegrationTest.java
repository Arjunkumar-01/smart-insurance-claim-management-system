package com.talentfarm.sicms.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ProductIntegrationTest {

    private static final String PASSWORD = "Valid!Pass123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void administratorCanCreateUpdateSearchAndCompareProducts() throws Exception {
        String adminToken = tokenForRole(ApplicationRole.ADMINISTRATOR);
        String firstCode = "HLT-" + System.nanoTime();
        String secondCode = "LIF-" + System.nanoTime();
        UUID firstId = createProduct(adminToken, firstCode, "Health Secure");
        UUID secondId = createProduct(adminToken, secondCode, "Life Secure");

        mockMvc.perform(get("/api/products/" + firstId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value(firstCode))
                .andExpect(jsonPath("$.premiumAmount").value(125.50))
                .andExpect(jsonPath("$.tenureMonths").value(12));

        mockMvc.perform(get("/api/products").header("Authorization", bearer(adminToken))
                        .queryParam("name", "Health").queryParam("category", "health"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.products", hasSize(1)))
                .andExpect(jsonPath("$.products[0].category").value("HEALTH"));

        mockMvc.perform(put("/api/products/" + firstId).header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson("Health Secure Plus", "ACTIVE")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Health Secure Plus"));

        mockMvc.perform(get("/api/products/compare").header("Authorization", bearer(adminToken))
                        .queryParam("ids", firstId + "," + secondId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.products", hasSize(2)));

        var persisted = productRepository.findById(firstId).orElseThrow();
        assertEquals(ProductCategory.HEALTH, persisted.getCategory());
        assertEquals(ProductStatus.ACTIVE, persisted.getStatus());
        assertEquals(12, persisted.getTenureMonths());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertNotNull(persisted.getVersion());
    }

    @Test
    void customerAndAgentCanReadActiveProductsButCannotMutate() throws Exception {
        String adminToken = tokenForRole(ApplicationRole.ADMINISTRATOR);
        UUID productId = createProduct(adminToken, "VEH-" + System.nanoTime(), "Vehicle Secure");

        String customerToken = registerCustomer("product.customer." + System.nanoTime() + "@example.test");
        String agentToken = tokenForRole(ApplicationRole.AGENT);

        mockMvc.perform(get("/api/products/" + productId).header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/products").header("Authorization", bearer(agentToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/products").header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON).content(createJson("LIF-" + System.nanoTime(), "Blocked")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/products/" + productId).header("Authorization", bearer(agentToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson("Blocked", "ACTIVE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/products").header("Authorization", bearer(tokenForRole(ApplicationRole.CLAIMS_OFFICER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateInvalidAndInactiveProductRulesAreEnforced() throws Exception {
        String adminToken = tokenForRole(ApplicationRole.ADMINISTRATOR);
        String code = "DUP-" + System.nanoTime();
        UUID productId = createProduct(adminToken, code, "Duplicate Candidate");

        mockMvc.perform(post("/api/products").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(createJson(code.toLowerCase(), "Duplicate")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("product code")));

        mockMvc.perform(post("/api/products").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(createJson("BAD-" + System.nanoTime(), "")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/products").header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(createJson("TERM-" + System.nanoTime(), "Invalid")
                                .replace("\"tenureMonths\":12", "\"tenureMonths\":0")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/products/" + productId).header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson("Withdrawn", "INACTIVE")))
                .andExpect(status().isOk());

        String customerToken = registerCustomer("inactive.customer." + System.nanoTime() + "@example.test");
        mockMvc.perform(get("/api/products/" + productId).header("Authorization", bearer(customerToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/products").header("Authorization", bearer(customerToken)).queryParam("status", "INACTIVE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.products", hasSize(0)));
        mockMvc.perform(get("/api/products/compare").header("Authorization", bearer(customerToken))
                        .queryParam("ids", productId + "," + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private UUID createProduct(String token, String code, String name) throws Exception {
        String response = mockMvc.perform(post("/api/products").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(createJson(code, name)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("productId").asText());
    }

    private String registerCustomer(String email) throws Exception {
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

    private String tokenForRole(ApplicationRole role) {
        ApplicationRoleEntity roleEntity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + "." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(roleEntity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String createJson(String code, String name) {
        return "{\"productCode\":\"" + code + "\",\"name\":\"" + name + "\",\"description\":\"Synthetic product\","
                + "\"category\":\"HEALTH\",\"coverageDescription\":\"Synthetic medical coverage\","
                + "\"coverageAmount\":\"100000.00\",\"premiumAmount\":\"125.50\",\"tenureMonths\":12}";
    }

    private String updateJson(String name, String status) {
        return "{\"name\":\"" + name + "\",\"description\":\"Updated synthetic product\",\"category\":\"HEALTH\","
                + "\"coverageDescription\":\"Updated synthetic coverage\",\"coverageAmount\":\"120000.00\","
                + "\"premiumAmount\":\"130.50\",\"tenureMonths\":12,\"status\":\"" + status + "\"}";
    }

    private String bearer(String token) { return "Bearer " + token; }
}