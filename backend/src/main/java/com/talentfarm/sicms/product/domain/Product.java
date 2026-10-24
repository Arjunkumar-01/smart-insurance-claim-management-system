package com.talentfarm.sicms.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "product_code", nullable = false, unique = true, length = 20)
    private String productCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(name = "coverage_description", nullable = false, length = 2000)
    private String coverageDescription;

    @Column(name = "coverage_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "premium_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Product() {
    }

    public Product(String productCode, String name, String description, ProductCategory category,
                   String coverageDescription, BigDecimal coverageAmount, BigDecimal premiumAmount,
                   int tenureMonths, ProductStatus status) {
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.coverageDescription = coverageDescription;
        this.coverageAmount = coverageAmount;
        this.premiumAmount = premiumAmount;
        this.tenureMonths = tenureMonths;
        this.status = status;
    }

    public void update(String name, String description, ProductCategory category, String coverageDescription,
                       BigDecimal coverageAmount, BigDecimal premiumAmount, int tenureMonths, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.coverageDescription = coverageDescription;
        this.coverageAmount = coverageAmount;
        this.premiumAmount = premiumAmount;
        this.tenureMonths = tenureMonths;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductCategory getCategory() { return category; }
    public String getCoverageDescription() { return coverageDescription; }
    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public Integer getTenureMonths() { return tenureMonths; }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}