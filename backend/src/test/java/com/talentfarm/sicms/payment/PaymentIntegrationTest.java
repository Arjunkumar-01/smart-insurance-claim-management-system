package com.talentfarm.sicms.payment;

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
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentIntegrationTest {
    private static final String PASSWORD = "Valid!Pass123";
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationRoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Test
    void customerPaymentUsesPolicySnapshotAndCreatesInvoiceAndReceipt() throws Exception {
        TestPolicy setup = customerWithPolicy("payment.owner." + System.nanoTime() + "@example.test", new BigDecimal("125.50"));
        String customer = setup.token();
        String policyId = setup.policyId();
        String payment = mockMvc.perform(post("/api/payments").header("Authorization", bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + policyId + "\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(125.50))
                .andExpect(jsonPath("$.invoice.invoiceNumber").isString())
                .andExpect(jsonPath("$.receipt.receiptNumber").isString())
                .andReturn().getResponse().getContentAsString();
        String paymentId = objectMapper.readTree(payment).get("paymentId").asText();
        mockMvc.perform(get("/api/payments/me").header("Authorization", bearer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payments[0].paymentId").value(paymentId));
        mockMvc.perform(get("/api/payments/me/" + paymentId + "/invoice").header("Authorization", bearer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(125.50));
        mockMvc.perform(get("/api/payments/me/" + paymentId + "/receipt").header("Authorization", bearer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.receiptNumber").isString());
        mockMvc.perform(get("/api/payments/me/" + paymentId + "/receipt/download").header("Authorization", bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals(MediaType.APPLICATION_PDF_VALUE,
                        result.getResponse().getContentType()))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getHeader("Content-Disposition").startsWith("attachment; filename=\"sicms-receipt-")))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        new String(result.getResponse().getContentAsByteArray(), 0, 4).startsWith("%PDF")));
        mockMvc.perform(post("/api/payments").header("Authorization", bearer(customer)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + policyId + "\",\"paymentMethod\":\"UPI\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString("already been paid")));
    }

    @Test
    void otherRolesAndOtherCustomersCannotAccessPaymentOperations() throws Exception {
        TestPolicy ownerSetup = customerWithPolicy("payment.other.owner." + System.nanoTime() + "@example.test", new BigDecimal("50.00"));
        String owner = ownerSetup.token();
        String other = customerWithPolicy("payment.other.customer." + System.nanoTime() + "@example.test", new BigDecimal("60.00")).token();
        String policyId = ownerSetup.policyId();
        String agent = tokenForRole(ApplicationRole.AGENT);
        String claims = tokenForRole(ApplicationRole.CLAIMS_OFFICER);
        mockMvc.perform(post("/api/payments").header("Authorization", bearer(other)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + policyId + "\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/payments/me").header("Authorization", bearer(agent))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/payments/me").header("Authorization", bearer(claims))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/payments/me")).andExpect(status().isUnauthorized());
    }

        private TestPolicy customerWithPolicy(String email, BigDecimal premium) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Synthetic\",\"lastName\":\"Customer\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();
        mockMvc.perform(post("/api/customers/me/profile").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+15550101400\",\"dateOfBirth\":\"1990-01-01\",\"addressLine1\":\"1 Payment Street\",\"city\":\"Testville\",\"state\":\"Test\",\"postalCode\":\"10000\",\"country\":\"Exampleland\"}"))
                .andExpect(status().isCreated());
        Product product = productRepository.saveAndFlush(new Product("P" + (System.nanoTime() % 1000000000), "Payment Product", "Synthetic",
                ProductCategory.HEALTH, "Synthetic coverage", new BigDecimal("100000.00"), premium, 12, ProductStatus.ACTIVE));
        String policy = mockMvc.perform(post("/api/policies").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + product.getId() + "\",\"startDate\":\"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new TestPolicy(token, objectMapper.readTree(policy).get("policyId").asText());
    }

    private record TestPolicy(String token, String policyId) { }

    private String tokenForRole(ApplicationRole role) {
        ApplicationRoleEntity entity = roleRepository.findByName(role).orElseThrow();
        User user = new User(role.name().toLowerCase() + ".payment." + System.nanoTime() + "@example.test",
                passwordEncoder.encode(PASSWORD), "Synthetic", role.name());
        user.addRole(entity);
        return jwtTokenProvider.generateAccessToken(CustomUserDetails.from(userRepository.saveAndFlush(user))).value();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
