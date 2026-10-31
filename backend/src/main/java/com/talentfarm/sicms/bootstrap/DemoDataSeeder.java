package com.talentfarm.sicms.bootstrap;

import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.claim.domain.Claim;
import com.talentfarm.sicms.claim.domain.ClaimDocument;
import com.talentfarm.sicms.claim.domain.ClaimType;
import com.talentfarm.sicms.claim.dto.request.ReviewDecisionRequest;
import com.talentfarm.sicms.claim.dto.request.SettleClaimRequest;
import com.talentfarm.sicms.claim.dto.request.SubmitClaimRequest;
import com.talentfarm.sicms.claim.repository.ClaimDocumentRepository;
import com.talentfarm.sicms.claim.repository.ClaimRepository;
import com.talentfarm.sicms.claim.service.ClaimReviewService;
import com.talentfarm.sicms.claim.service.ClaimService;
import com.talentfarm.sicms.claim.service.ClaimSettlementService;
import com.talentfarm.sicms.customer.domain.Customer;
import com.talentfarm.sicms.customer.domain.CustomerKyc;
import com.talentfarm.sicms.customer.repository.CustomerKycRepository;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.payment.domain.PaymentMethod;
import com.talentfarm.sicms.payment.dto.request.MakePaymentRequest;
import com.talentfarm.sicms.payment.service.PaymentService;
import com.talentfarm.sicms.policy.domain.Policy;
import com.talentfarm.sicms.policy.dto.request.RequestPolicyCancellationRequest;
import com.talentfarm.sicms.policy.dto.request.UpdateNomineeRequest;
import com.talentfarm.sicms.policy.repository.PolicyRepository;
import com.talentfarm.sicms.policy.service.PolicyService;
import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.dto.request.CreateProductRequest;
import com.talentfarm.sicms.product.dto.request.UpdateProductRequest;
import com.talentfarm.sicms.product.repository.ProductRepository;
import com.talentfarm.sicms.product.service.ProductService;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.storage.model.StoredFile;
import com.talentfarm.sicms.storage.service.StorageService;
import com.talentfarm.sicms.user.domain.AccountStatus;
import com.talentfarm.sicms.user.domain.ApplicationRole;
import com.talentfarm.sicms.user.domain.ApplicationRoleEntity;
import com.talentfarm.sicms.user.domain.User;
import com.talentfarm.sicms.user.repository.ApplicationRoleRepository;
import com.talentfarm.sicms.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@Profile({"local", "dev", "demo"})
@ConditionalOnProperty(name = "sicms.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final byte[] PDF_DOCUMENT_CONTENT = "%PDF-1.4\n% SICMS synthetic demonstration document\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF\n".getBytes();
    private static final byte[] JPEG_DOCUMENT_CONTENT = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 74, 70, 73, 70, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0,
            (byte) 0xFF, (byte) 0xD9
    };
    private static final List<String> RESET_TABLES = List.of(
            "claim_settlements", "claim_status_history", "claim_reviews", "claim_documents", "claims",
            "receipts", "payments", "invoices", "policy_cancellation_requests", "policy_nominees",
            "policies", "customer_kyc", "customers", "audit_logs", "user_roles", "app_users", "products");

    private final DemoDataProperties properties;
    private final Environment environment;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ApplicationRoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final CustomerKycRepository customerKycRepository;
    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;
    private final PolicyService policyService;
    private final PaymentService paymentService;
    private final ClaimService claimService;
    private final ClaimReviewService claimReviewService;
    private final ClaimSettlementService claimSettlementService;
    private final AuditService auditService;

    public DemoDataSeeder(DemoDataProperties properties, Environment environment, DataSource dataSource,
                          JdbcTemplate jdbcTemplate, StorageService storageService, UserRepository userRepository,
                          ApplicationRoleRepository roleRepository, CustomerRepository customerRepository,
                          CustomerKycRepository customerKycRepository, ProductRepository productRepository,
                          PolicyRepository policyRepository, ClaimRepository claimRepository,
                          ClaimDocumentRepository claimDocumentRepository, PasswordEncoder passwordEncoder,
                          ProductService productService, PolicyService policyService, PaymentService paymentService,
                          ClaimService claimService, ClaimReviewService claimReviewService,
                          ClaimSettlementService claimSettlementService, AuditService auditService) {
        this.properties = properties;
        this.environment = environment;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.customerKycRepository = customerKycRepository;
        this.productRepository = productRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.claimDocumentRepository = claimDocumentRepository;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
        this.policyService = policyService;
        this.paymentService = paymentService;
        this.claimService = claimService;
        this.claimReviewService = claimReviewService;
        this.claimSettlementService = claimSettlementService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateSafety();
        resetBusinessData();

        User customerUser = createUser("maya.thompson@example.test", "Maya", "Thompson",
                ApplicationRole.CUSTOMER, properties.customerPassword(), AccountStatus.ACTIVE);
        User agent = createUser("ethan.brooks@example.test", "Ethan", "Brooks",
                ApplicationRole.AGENT, properties.agentPassword(), AccountStatus.ACTIVE);
        User officer = createUser("priya.raman@example.test", "Priya", "Raman",
                ApplicationRole.CLAIMS_OFFICER, properties.claimsOfficerPassword(), AccountStatus.ACTIVE);
        createUser("daniel.mercer@example.test", "Daniel", "Mercer",
                ApplicationRole.ADMINISTRATOR, properties.administratorPassword(), AccountStatus.ACTIVE);
        createUser("sophia.lee@example.test", "Sophia", "Lee",
                ApplicationRole.CUSTOMER, properties.customerPassword(), AccountStatus.INACTIVE);

        Customer customer = customerRepository.saveAndFlush(new Customer(customerUser, "+1 202 555 0147",
                LocalDate.of(1988, 6, 14), "24 Harbor View Lane", "Suite 8", "Lakeside", "Washington",
                "98109", "United States"));
        CustomerKyc kyc = new CustomerKyc(customer, "DEMO_MEMBER_ID", "SICMS-DEMO-7842");
        kyc.verify();
        customerKycRepository.saveAndFlush(kyc);

        Product healthEssential = createProduct("HSE-ESS-12", "HealthSecure Essential", "Everyday health protection for individuals.",
                "HEALTH", "Outpatient care and diagnostic expenses.", "50000", "720", 12);
        Product healthPremier = createProduct("HSE-PRM-12", "HealthSecure Premier", "Enhanced health protection with higher limits.",
                "HEALTH", "Outpatient care, diagnostics, and specialist treatment.", "150000", "1450", 12);
        Product lifeTerm = createProduct("LGT-TERM-24", "LifeGuard Term Shield", "Straightforward life coverage for long-term security.",
                "LIFE", "Fixed life cover for the named policyholder.", "300000", "980", 24);
        Product lifeFamily = createProduct("LGT-FAM-24", "LifeGuard Family Protect", "Family-focused life protection with broader cover.",
                "LIFE", "Expanded life cover for family financial protection.", "500000", "1560", 24);
        Product vehicleComprehensive = createProduct("DRA-CMP-12", "DriveAssure Comprehensive", "Comprehensive vehicle protection for daily driving.",
                "VEHICLE", "Collision damage, roadside support, and third-party cover.", "75000", "860", 12);
        Product vehicleStandard = createProduct("DRA-STD-12", "DriveAssure Standard", "Essential vehicle protection with clear limits.",
                "VEHICLE", "Collision damage assessment and third-party cover.", "40000", "540", 12);
        Product travelEssential = createProduct("TSA-ESS-06", "TravelSafe Essential", "Travel cover for short international journeys.",
                "TRAVEL", "Baggage delay and emergency travel assistance.", "25000", "220", 6);
        productService.update(travelEssential.getId(), new UpdateProductRequest(travelEssential.getName(),
                travelEssential.getDescription(), travelEssential.getCategory().name(), travelEssential.getCoverageDescription(),
                travelEssential.getCoverageAmount(), travelEssential.getPremiumAmount(), travelEssential.getTenureMonths(), "INACTIVE"));

        LocalDate today = LocalDate.now();
        Policy paidPolicy = savePolicy(customer, healthPremier, today.minusMonths(5));
        Policy reviewPolicy = savePolicy(customer, vehicleComprehensive, today.minusMonths(4));
        Policy expiredPolicy = savePolicy(customer, lifeTerm, today.minusMonths(30));
        expiredPolicy.expire();
        policyRepository.saveAndFlush(expiredPolicy);
        Policy cancellationPolicy = savePolicy(customer, lifeFamily, today.minusMonths(3));
        Policy submittedPolicy = savePolicy(customer, healthEssential, today.minusMonths(2));
        auditService.record(AuditAction.POLICY_PURCHASED, customerUser, "POLICY", paidPolicy.getId(), "Demo policy purchased");
        auditService.record(AuditAction.POLICY_PURCHASED, customerUser, "POLICY", reviewPolicy.getId(), "Demo policy purchased");
        auditService.record(AuditAction.POLICY_PURCHASED, customerUser, "POLICY", cancellationPolicy.getId(), "Demo policy purchased");

        Authentication customerAuthentication = authentication(customerUser);
        Authentication officerAuthentication = authentication(officer);
        policyService.updateNominee(customerAuthentication, paidPolicy.getId(),
                new UpdateNomineeRequest("Jordan Ellis", "Spouse", "+1 202 555 0189"));
        paymentService.pay(customerAuthentication, new MakePaymentRequest(paidPolicy.getId(), PaymentMethod.CARD));
        policyService.requestCancellation(customerAuthentication, cancellationPolicy.getId(),
                new RequestPolicyCancellationRequest("Demo cancellation review request"));

        Claim submitted = submit(customerAuthentication, submittedPolicy, ClaimType.HEALTH, "1850",
                "Outpatient procedure and diagnostic expenses.");
        storeDocument(submitted, "claim-incident-summary.pdf");

        Claim documentsRequired = submit(customerAuthentication, reviewPolicy, ClaimType.VEHICLE, "4200",
                "Vehicle collision damage assessment.");
        claimReviewService.start(officerAuthentication, documentsRequired.getId(), new ReviewDecisionRequest("Initial review completed."));
        claimReviewService.requestDocuments(officerAuthentication, documentsRequired.getId(),
                new ReviewDecisionRequest("Please provide a repair estimate for the assessment."));
        storeDocument(documentsRequired, "vehicle-damage-overview.jpg");

        Claim approved = submit(customerAuthentication, paidPolicy, ClaimType.HEALTH, "2400",
                "Routine outpatient treatment and diagnostic expenses.");
        claimReviewService.start(officerAuthentication, approved.getId(), new ReviewDecisionRequest("Coverage and policy eligibility confirmed."));
        claimReviewService.approve(officerAuthentication, approved.getId(), new ReviewDecisionRequest("Approved for settlement."));

        Claim settled = submit(customerAuthentication, cancellationPolicy, ClaimType.LIFE, "12500",
                "Fictional policy benefit request for demonstration.");
        claimReviewService.start(officerAuthentication, settled.getId(), new ReviewDecisionRequest("Documentation reviewed."));
        claimReviewService.approve(officerAuthentication, settled.getId(), new ReviewDecisionRequest("Approved for payment."));
        claimSettlementService.settle(officerAuthentication, settled.getId(), new SettleClaimRequest("Synthetic demo settlement completed."));

        Claim rejected = submit(customerAuthentication, reviewPolicy, ClaimType.VEHICLE, "7800",
                "Vehicle collision damage assessment with unsupported requested component.");
        claimReviewService.start(officerAuthentication, rejected.getId(), new ReviewDecisionRequest("Eligibility review started."));
        claimReviewService.reject(officerAuthentication, rejected.getId(),
                new ReviewDecisionRequest("Requested component is outside the selected coverage."));
        storeDocument(approved, "expense-summary.pdf");

        log.info("Synthetic demo data reset completed: users={}, products={}, policies={}, claims={}.",
                jdbcTemplate.queryForObject("select count(*) from app_users", Long.class),
                jdbcTemplate.queryForObject("select count(*) from products", Long.class),
                jdbcTemplate.queryForObject("select count(*) from policies", Long.class),
                jdbcTemplate.queryForObject("select count(*) from claims", Long.class));
    }

    private void validateSafety() {
        if (!properties.reset()) {
            throw new IllegalStateException("Demo data seeding requires sicms.demo-data.reset=true.");
        }
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(this::isProductionLike)) {
            throw new IllegalStateException("Demo data reset is forbidden with a production-like Spring profile.");
        }
        String url = databaseUrl();
        URI target = URI.create(url.substring("jdbc:".length()));
        String host = target.getHost();
        String database = target.getPath() == null ? "" : target.getPath().replaceFirst("^/", "");
        if (!"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equals(host) && !"::1".equals(host)) {
            throw new IllegalStateException("Demo data reset is restricted to a local PostgreSQL host.");
        }
        if (properties.expectedDatabase() == null || !properties.expectedDatabase().equals(database)) {
            throw new IllegalStateException("Configured demo database does not match the connected database.");
        }
        requirePassword(properties.customerPassword(), "customer");
        requirePassword(properties.agentPassword(), "agent");
        requirePassword(properties.claimsOfficerPassword(), "claims officer");
        requirePassword(properties.administratorPassword(), "administrator");
        log.info("Demo reset target confirmed: profile={}, host={}, database={}, reset=true.",
                String.join(",", environment.getActiveProfiles()), host, database);
    }

    private void resetBusinessData() {
        List<String> storageKeys = jdbcTemplate.queryForList("select storage_key from claim_documents", String.class);
        storageKeys.forEach(storageService::delete);
        RESET_TABLES.forEach(table -> jdbcTemplate.update("delete from " + table));
        log.info("Demo reset removed application business data from {} tables; Flyway history and application roles were preserved.",
                RESET_TABLES.size());
    }

    private User createUser(String email, String firstName, String lastName, ApplicationRole role, String password, AccountStatus status) {
        ApplicationRoleEntity roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new IllegalStateException("Required role is missing: " + role));
        User user = new User(email, passwordEncoder.encode(password), firstName, lastName);
        user.addRole(roleEntity);
        user.changeAccountStatus(status);
        return userRepository.saveAndFlush(user);
    }

    private Product createProduct(String code, String name, String description, String category, String coverageDescription,
                                  String coverage, String premium, int tenureMonths) {
        UUID id = productService.create(new CreateProductRequest(code, name, description, category, coverageDescription,
                money(coverage), money(premium), tenureMonths)).productId();
        return productRepository.findById(id).orElseThrow();
    }

    private Policy savePolicy(Customer customer, Product product, LocalDate startDate) {
        Policy policy = new Policy(number("POL"), customer, product, product.getPremiumAmount(), product.getCoverageAmount(),
                startDate, startDate.plusMonths(product.getTenureMonths()));
        return policyRepository.saveAndFlush(policy);
    }

    private Claim submit(Authentication customer, Policy policy, ClaimType claimType, String amount, String description) {
        return claimRepository.findById(claimService.submit(customer, new SubmitClaimRequest(policy.getId(), money(amount), claimType,
                LocalDate.now().minusDays(7), description)).claimId()).orElseThrow();
    }

    private void storeDocument(Claim claim, String originalName) {
        boolean jpeg = originalName.toLowerCase(Locale.ROOT).endsWith(".jpg");
        byte[] content = jpeg ? JPEG_DOCUMENT_CONTENT : PDF_DOCUMENT_CONTENT;
        StoredFile stored = storageService.store(claim.getId().toString(), originalName,
                jpeg ? "image/jpeg" : "application/pdf", (long) content.length, new ByteArrayInputStream(content));
        claimDocumentRepository.saveAndFlush(new ClaimDocument(claim, stored.originalFileName(), stored.storedFileName(),
                stored.contentType(), stored.fileSize(), stored.storageKey()));
    }

    private Authentication authentication(User user) {
        CustomUserDetails details = CustomUserDetails.from(user);
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    private String databaseUrl() {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to confirm the demo database target.", exception);
        }
    }

    private boolean isProductionLike(String profile) {
        return profile.toLowerCase(Locale.ROOT).contains("prod");
    }

    private void requirePassword(String password, String account) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Demo " + account + " password environment variable is required.");
        }
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value + ".00");
    }

    private String number(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    }
}
