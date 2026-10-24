package com.talentfarm.sicms.report.service;

import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.customer.repository.CustomerRepository;
import com.talentfarm.sicms.policy.domain.PolicyStatus;
import com.talentfarm.sicms.report.dto.response.AdminOverviewResponse;
import com.talentfarm.sicms.report.dto.response.ClaimStatusCount;
import com.talentfarm.sicms.report.dto.response.CustomerReportResponse;
import com.talentfarm.sicms.report.dto.response.MonthlyRevenueResponse;
import com.talentfarm.sicms.report.dto.response.ProductPerformanceResponse;
import com.talentfarm.sicms.report.dto.response.SettlementTrendResponse;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {
    private final EntityManager entityManager;
    private final CustomerRepository customerRepository;

    public ReportService(EntityManager entityManager, CustomerRepository customerRepository) {
        this.entityManager = entityManager;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerReportResponse customerSummary(Authentication authentication) {
        UUID customerId = currentCustomerId(authentication);
        long policies = count("select count(p) from Policy p where p.customer.id = :id", customerId);
        long activePolicies = count("select count(p) from Policy p where p.customer.id = :id and p.status = com.talentfarm.sicms.policy.domain.PolicyStatus.ACTIVE", customerId);
        long expiredPolicies = count("select count(p) from Policy p where p.customer.id = :id and p.status = com.talentfarm.sicms.policy.domain.PolicyStatus.EXPIRED", customerId);
        long claims = count("select count(c) from Claim c where c.policy.customer.id = :id", customerId);
        List<ClaimStatusCount> statuses = claimStatusCounts("where c.policy.customer.id = :id", customerId);
        List<Object[]> payments = entityManager.createQuery("select count(p), coalesce(sum(p.amount), 0) from Payment p where p.policy.customer.id = :id and p.status = com.talentfarm.sicms.payment.domain.PaymentStatus.COMPLETED", Object[].class).setParameter("id", customerId).getResultList();
        BigDecimal premium = money(payments.get(0)[1]);
        BigDecimal settlements = money(entityManager.createQuery("select coalesce(sum(s.settlementAmount), 0) from ClaimSettlement s where s.claim.policy.customer.id = :id", BigDecimal.class).setParameter("id", customerId).getSingleResult());
        return new CustomerReportResponse(policies, activePolicies, expiredPolicies, claims, statuses,
            ((Number) payments.get(0)[0]).longValue(), premium, settlements);
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse adminOverview() {
        List<AdminOverviewResponse.PolicyStatusCount> policies = Arrays.stream(PolicyStatus.values()).map(status -> new AdminOverviewResponse.PolicyStatusCount(status,
            entityManager.createQuery("select count(p) from Policy p where p.status = :status", Long.class)
                .setParameter("status", status).getSingleResult())).toList();
        List<ClaimStatusCount> claims = claimStatusCounts("", null);
        long totalClaims = claims.stream().mapToLong(ClaimStatusCount::count).sum();
        long approvedClaims = statusCount(claims, ClaimStatus.APPROVED);
        long rejectedClaims = statusCount(claims, ClaimStatus.REJECTED);
        long pendingClaims = statusCount(claims, ClaimStatus.SUBMITTED)
            + statusCount(claims, ClaimStatus.UNDER_REVIEW)
            + statusCount(claims, ClaimStatus.DOCUMENTS_REQUIRED);
        BigDecimal premium = money(entityManager.createQuery("select coalesce(sum(p.amount), 0) from Payment p where p.status = com.talentfarm.sicms.payment.domain.PaymentStatus.COMPLETED", BigDecimal.class).getSingleResult());
        BigDecimal settlements = money(entityManager.createQuery("select coalesce(sum(s.settlementAmount), 0) from ClaimSettlement s", BigDecimal.class).getSingleResult());
        BigDecimal ratio = premium.signum() == 0 ? BigDecimal.ZERO : settlements.multiply(BigDecimal.valueOf(100)).divide(premium, 2, RoundingMode.HALF_UP);
        return new AdminOverviewResponse(customerRepository.count(), policies, claims, totalClaims, approvedClaims,
            rejectedClaims, pendingClaims, premium, settlements, ratio);
    }

    @Transactional(readOnly = true)
    public List<MonthlyRevenueResponse> monthlyRevenue(int year) {
        Instant from = YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = YearMonth.of(year + 1, 1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> rows = entityManager.createQuery("select p.paidAt, p.amount from Payment p where p.status = com.talentfarm.sicms.payment.domain.PaymentStatus.COMPLETED and p.paidAt >= :from and p.paidAt < :to", Object[].class).setParameter("from", from).setParameter("to", to).getResultList();
        Map<YearMonth, List<Object[]>> grouped = rows.stream().collect(java.util.stream.Collectors.groupingBy(row -> YearMonth.from(((Instant) row[0]).atZone(ZoneOffset.UTC))));
        return grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> new MonthlyRevenueResponse(entry.getKey().getYear(), entry.getKey().getMonthValue(), entry.getValue().size(), money(entry.getValue().stream().map(row -> (BigDecimal) row[1]).reduce(BigDecimal.ZERO, BigDecimal::add)))).toList();
    }

        @Transactional(readOnly = true)
        public List<SettlementTrendResponse> settlementTrend(int year) {
        Instant from = YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = YearMonth.of(year + 1, 1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> rows = entityManager.createQuery("select s.settledAt, s.settlementAmount from ClaimSettlement s where s.settledAt >= :from and s.settledAt < :to", Object[].class)
            .setParameter("from", from).setParameter("to", to).getResultList();
        Map<YearMonth, List<Object[]>> grouped = rows.stream().collect(java.util.stream.Collectors.groupingBy(
            row -> YearMonth.from(((Instant) row[0]).atZone(ZoneOffset.UTC))));
        return grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> new SettlementTrendResponse(
            entry.getKey().getYear(), entry.getKey().getMonthValue(), entry.getValue().size(),
            money(entry.getValue().stream().map(row -> (BigDecimal) row[1]).reduce(BigDecimal.ZERO, BigDecimal::add)))).toList();
        }

    @Transactional(readOnly = true)
    public List<ProductPerformanceResponse> productPerformance() {
        List<Object[]> products = entityManager.createQuery("select p.id, p.productCode, p.name from Product p order by p.productCode", Object[].class).getResultList();
        Map<UUID, Long> policyCounts = groupedLong("select p.product.id, count(p) from Policy p group by p.product.id");
        Map<UUID, Long> claimCounts = groupedLong("select c.policy.product.id, count(c) from Claim c group by c.policy.product.id");
        Map<UUID, BigDecimal> premiums = groupedMoney("select p.policy.product.id, coalesce(sum(p.amount), 0) from Payment p where p.status = com.talentfarm.sicms.payment.domain.PaymentStatus.COMPLETED group by p.policy.product.id");
        Map<UUID, BigDecimal> settlements = groupedMoney("select s.claim.policy.product.id, coalesce(sum(s.settlementAmount), 0) from ClaimSettlement s group by s.claim.policy.product.id");
        return products.stream().map(row -> {
            UUID id = (UUID) row[0];
            return new ProductPerformanceResponse(id, (String) row[1], (String) row[2], policyCounts.getOrDefault(id, 0L),
                    claimCounts.getOrDefault(id, 0L), premiums.getOrDefault(id, BigDecimal.ZERO.setScale(2)),
                    settlements.getOrDefault(id, BigDecimal.ZERO.setScale(2)));
        }).toList();
    }

    private List<ClaimStatusCount> claimStatusCounts(String suffix, UUID id) {
        String query = "select c.status, count(c) from Claim c " + suffix + " group by c.status";
        var typed = entityManager.createQuery(query, Object[].class);
        if (id != null) typed.setParameter("id", id);
        Map<ClaimStatus, Long> values = new EnumMap<>(ClaimStatus.class);
        for (Object[] row : typed.getResultList()) values.put((ClaimStatus) row[0], ((Number) row[1]).longValue());
        return Arrays.stream(ClaimStatus.values()).map(status -> new ClaimStatusCount(status, values.getOrDefault(status, 0L))).toList();
    }

    private long count(String query, Object id) { return ((Number) entityManager.createQuery(query).setParameter("id", id).getSingleResult()).longValue(); }
    private long statusCount(List<ClaimStatusCount> counts, ClaimStatus status) {
        return counts.stream().filter(count -> count.status() == status).mapToLong(ClaimStatusCount::count).findFirst().orElse(0L);
    }
    private UUID currentCustomerId(Authentication authentication) { return customerRepository.findByUserId(((CustomUserDetails) authentication.getPrincipal()).getUserId()).orElseThrow().getId(); }
    private BigDecimal money(Object value) { return value == null ? BigDecimal.ZERO.setScale(2) : new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP); }
    private Map<UUID, Long> groupedLong(String query) {
        Map<UUID, Long> values = new java.util.HashMap<>();
        for (Object[] row : entityManager.createQuery(query, Object[].class).getResultList()) values.put((UUID) row[0], ((Number) row[1]).longValue());
        return values;
    }
    private Map<UUID, BigDecimal> groupedMoney(String query) {
        Map<UUID, BigDecimal> values = new java.util.HashMap<>();
        for (Object[] row : entityManager.createQuery(query, Object[].class).getResultList()) values.put((UUID) row[0], money(row[1]));
        return values;
    }
}