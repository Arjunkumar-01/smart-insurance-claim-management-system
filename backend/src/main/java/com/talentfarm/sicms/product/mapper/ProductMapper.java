package com.talentfarm.sicms.product.mapper;

import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getProductCode(), product.getName(), product.getDescription(),
                product.getCategory(), product.getCoverageDescription(), product.getCoverageAmount(),
                product.getPremiumAmount(), product.getTenureMonths(), product.getStatus(),
                product.getCreatedAt(), product.getUpdatedAt());
    }
}