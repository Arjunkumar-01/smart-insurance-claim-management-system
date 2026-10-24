package com.talentfarm.sicms.product.dto.response;

import com.talentfarm.sicms.product.domain.ProductCategory;
import com.talentfarm.sicms.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(UUID productId, String productCode, String name, String description,
                              ProductCategory category, String coverageDescription, BigDecimal coverageAmount,
                              BigDecimal premiumAmount, Integer tenureMonths, ProductStatus status,
                              Instant createdAt, Instant updatedAt) {
}