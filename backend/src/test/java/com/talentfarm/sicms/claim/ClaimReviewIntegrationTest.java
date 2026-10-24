package com.talentfarm.sicms.claim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.domain.ProductCategory;
import com.talentfarm.sicms.product.domain.ProductStatus;
import com.talentfarm.sicms.product.repository.ProductRepository;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.storage.model.StoredFile;
import com.talentfarm.sicms.storage.service.StorageService;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimReviewIntegrationTest {
    private static final String PASSWORD = "Valid!Pass123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StorageService storageService;

    @Test
    void officerCanMoveClaimThroughDocumentRequestResumeAndApproval() throws Exception {
        TestClaim setup = customerClaim("review.customer." + System.nanoTime() + "@example.test");
        String officer = roleToken(ApplicationRole.CLAIMS_OFFICER);

        mockMvc.perform(get("/api/claims/review").header("Authorization", bearer(officer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.claims.length()", greaterThanOrEqualTo(1)));
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/start").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Initial review\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/request-documents").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Please provide supporting evidence\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DOCUMENTS_REQUIRED"));
        mockMvc.perform(post("/api/claims/me/" + setup.claimId() + "/documents").header("Authorization", bearer(setup.customerToken()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content(""))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/resume").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Documents reviewed\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/approve").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Claim approved\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(get("/api/claims/me/" + setup.claimId() + "/history").header("Authorization", bearer(setup.customerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("[0].fromStatus").doesNotExist())
                .andExpect(jsonPath("[0].toStatus").value("SUBMITTED"))
                .andExpect(jsonPath("[4].toStatus").value("APPROVED"));
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/reject").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Invalid after approval\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/claims/me/" + setup.claimId()).header("Authorization", bearer(setup.customerToken())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void invalidTransitionsAndWrongRolesAreRejected() throws Exception {
        TestClaim setup = customerClaim("review.invalid." + System.nanoTime() + "@example.test");
        String officer = roleToken(ApplicationRole.CLAIMS_OFFICER);
        String agent = roleToken(ApplicationRole.AGENT);
        String admin = roleToken(ApplicationRole.ADMINISTRATOR);
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/approve").header("Authorization", bearer(officer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Invalid\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("transition")));
        mockMvc.perform(get("/api/claims/review").header("Authorization", bearer(setup.customerToken())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/start").header("Authorization", bearer(agent))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Denied\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/claims/review/" + setup.claimId() + "/start").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Denied\"}"))
                .andExpect(status().isForbidden());
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
                        .content("{\"phoneNumber\":\"+15550101300\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Review Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        Product product = productRepository.saveAndFlush(new Product("R" + (System.nanoTime() % 1000000000), "Review Product", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("100000.00"), new BigDecimal("10.00"), 12, ProductStatus.ACTIVE));
        String policy = mockMvc.perform(post("/api/policies").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + product.getId() + "\",\"startDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String claim = mockMvc.perform(post("/api/claims").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + objectMapper.readTree(policy).get("policyId").asText() + "\",\"claimAmount\":\"10.00\",\"claimType\":\"HEALTH\",\"incidentDate\":\"" + LocalDate.now() + "\",\"description\":\"Review claim\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new TestClaim(token, objectMapper.readTree(claim).get("claimId").asText());
    }

    private String roleToken(ApplicationRole role) {
        ApplicationRoleEntity entity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".review." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(entity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String bearer(String token) { return "Bearer " + token; }
    private record TestClaim(String customerToken, String claimId) { }
}
