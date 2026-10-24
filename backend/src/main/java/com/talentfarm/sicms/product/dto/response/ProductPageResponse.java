package com.talentfarm.sicms.product.dto.response;

import java.util.List;

public record ProductPageResponse(List<ProductResponse> products, int page, int size, long totalElements, int totalPages) {
}