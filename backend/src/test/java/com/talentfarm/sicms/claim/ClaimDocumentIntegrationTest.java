package com.talentfarm.sicms.claim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.domain.ProductCategory;
import com.talentfarm.sicms.product.domain.ProductStatus;
import com.talentfarm.sicms.product.repository.ProductRepository;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.storage.model.StoredFile;
import com.talentfarm.sicms.storage.service.StorageService;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.UserRepository;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimDocumentIntegrationTest {
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
    void customerCanUploadListAndDownloadOwnedDocumentWithoutStatusChange() throws Exception {
        TestClaim setup = customerWithClaim("document.owner." + System.nanoTime() + "@example.test");
        String token = setup.token();
        String claimId = setup.claimId();
        byte[] content = "synthetic document".getBytes();
        when(storageService.store(ArgumentMatchers.eq(claimId), ArgumentMatchers.anyString(),
                ArgumentMatchers.eq("application/pdf"), ArgumentMatchers.eq((long) content.length), ArgumentMatchers.any()))
                .thenReturn(new StoredFile("../../synthetic.pdf", "server-generated", claimId + "/server-generated", content.length, "application/pdf"));

        MockMultipartFile file = new MockMultipartFile("file", "../../synthetic.pdf", "application/pdf", content);
        String response = mockMvc.perform(multipart("/api/claims/me/{claimId}/documents", claimId)
                        .file(file).header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFileName").value("../../synthetic.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.storageKey").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(response);

        mockMvc.perform(get("/api/claims/me/{claimId}/documents", claimId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        when(storageService.load(claimId + "/server-generated")).thenReturn(new org.springframework.core.io.ByteArrayResource(content));
        mockMvc.perform(get("/api/claims/me/{claimId}/documents/{documentId}", claimId, document.get("documentId").asText())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void nonCustomerRolesCannotUseCustomerDocumentEndpoints() throws Exception {
        String token = roleToken(ApplicationRole.AGENT);
        mockMvc.perform(get("/api/claims/me/{claimId}/documents", java.util.UUID.randomUUID()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

        private TestClaim customerWithClaim(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Synthetic\",\"lastName\":\"Customer\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+15550101200\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Synthetic Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        Product product = productRepository.saveAndFlush(new Product("D" + (System.nanoTime() % 1000000000), "Document Product", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("100000.00"), new BigDecimal("10.00"), 12, ProductStatus.ACTIVE));
        String policy = mockMvc.perform(post("/api/policies").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + product.getId() + "\",\"startDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String claim = mockMvc.perform(post("/api/claims").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + objectMapper.readTree(policy).get("policyId").asText() + "\",\"claimAmount\":\"10.00\",\"claimType\":\"HEALTH\",\"incidentDate\":\"" + LocalDate.now() + "\",\"description\":\"Synthetic\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
                return new TestClaim(token, objectMapper.readTree(claim).get("claimId").asText());
    }

        private record TestClaim(String token, String claimId) { }

    private String roleToken(ApplicationRole role) {
        var roleEntity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".document." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(roleEntity);
        return jwtTokenProvider.generateAccessToken(com.talentfarm.sicms.security.service.CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }
}
