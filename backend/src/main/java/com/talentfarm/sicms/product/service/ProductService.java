package com.talentfarm.sicms.product.service;

import com.talentfarm.sicms.exception.DuplicateResourceException;
import com.talentfarm.sicms.audit.domain.AuditAction;
import com.talentfarm.sicms.audit.service.AuditService;
import com.talentfarm.sicms.exception.ResourceNotFoundException;
import com.talentfarm.sicms.product.domain.Product;
import com.talentfarm.sicms.product.domain.ProductCategory;
import com.talentfarm.sicms.product.domain.ProductStatus;
import com.talentfarm.sicms.product.dto.request.CreateProductRequest;
import com.talentfarm.sicms.product.dto.request.UpdateProductRequest;
import com.talentfarm.sicms.product.dto.response.ProductComparisonResponse;
import com.talentfarm.sicms.product.dto.response.ProductPageResponse;
import com.talentfarm.sicms.product.dto.response.ProductResponse;
import com.talentfarm.sicms.product.mapper.ProductMapper;
import com.talentfarm.sicms.product.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProductService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_COMPARISON_PRODUCTS = 4;

    private final ProductRepository productRepository;
    private final ProductMapper mapper;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, ProductMapper mapper, AuditService auditService) {
        this.productRepository = productRepository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String productCode = normalizeCode(request.productCode());
        if (productRepository.existsByProductCode(productCode)) {
            throw new DuplicateResourceException("A product with this product code already exists");
        }
        Product product = new Product(productCode, clean(request.name()), cleanNullable(request.description()),
                category(request.category()), clean(request.coverageDescription()), request.coverageAmount(),
            request.premiumAmount(), request.tenureMonths(), ProductStatus.ACTIVE);
        try {
            Product saved = productRepository.saveAndFlush(product);
            auditService.record(AuditAction.PRODUCT_CREATED, null, "PRODUCT", saved.getId(), "Product created");
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("A product with this product code already exists");
        }
    }

    @Transactional
    public ProductResponse update(UUID productId, UpdateProductRequest request) {
        Product product = find(productId);
        product.update(clean(request.name()), cleanNullable(request.description()), category(request.category()),
            clean(request.coverageDescription()), request.coverageAmount(), request.premiumAmount(),
            request.tenureMonths(), status(request.status()));
        auditService.record(AuditAction.PRODUCT_UPDATED, null, "PRODUCT", productId, "Product updated");
        return mapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID productId, Authentication authentication) {
        Product product = find(productId);
        if (!canSeeInactive(authentication) && product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product was not found");
        }
        return mapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductPageResponse list(Authentication authentication, String name, String productCode, String category,
                                    String status, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 50");
        }
        boolean includeInactive = canSeeInactive(authentication);
        ProductStatus requestedStatus = status == null || status.isBlank() ? null : status(status);
        if (!includeInactive && requestedStatus == ProductStatus.INACTIVE) {
            return emptyPage(page, size);
        }
        Specification<Product> specification = Specification.<Product>where(null)
                .and(equalsCategory(category))
                .and(equalsStatus(includeInactive ? requestedStatus : ProductStatus.ACTIVE))
                .and(containsIgnoreCase("name", name))
                .and(containsIgnoreCase("productCode", productCode));
        Page<Product> result = productRepository.findAll(specification, pageable(page, size, sort));
        return toPageResponse(result);
    }

    @Transactional(readOnly = true)
    public ProductComparisonResponse compare(Authentication authentication, String ids) {
        List<UUID> productIds = parseIds(ids);
        boolean includeInactive = canSeeInactive(authentication);
        List<Product> products = productIds.stream().map(this::find).toList();
        if (!includeInactive && products.stream().anyMatch(product -> product.getStatus() != ProductStatus.ACTIVE)) {
            throw new ResourceNotFoundException("One or more products were not found");
        }
        return new ProductComparisonResponse(products.stream().map(mapper::toResponse).toList());
    }

    private Product find(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product was not found"));
    }

    private boolean canSeeInactive(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRATOR"));
    }

    private Specification<Product> equalsCategory(String value) {
        if (value == null || value.isBlank()) return null;
        ProductCategory category = category(value);
        return (root, query, builder) -> builder.equal(root.get("category"), category);
    }

    private Specification<Product> equalsStatus(ProductStatus value) {
        if (value == null) return null;
        return (root, query, builder) -> builder.equal(root.get("status"), value);
    }

    private Specification<Product> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return null;
        String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, builder) -> builder.like(builder.lower(root.get(field)), pattern);
    }

    private Pageable pageable(int page, int size, String sort) {
        String[] values = sort == null || sort.isBlank() ? new String[]{"createdAt", "desc"} : sort.split(",", -1);
        String property = values[0];
        List<String> allowed = List.of("name", "productCode", "premiumAmount", "coverageAmount", "createdAt");
        if (!allowed.contains(property) || values.length > 2) {
            throw new IllegalArgumentException("Sort must use a supported Product field");
        }
        Sort.Direction direction = values.length == 2 ? Sort.Direction.fromOptionalString(values[1]).orElseThrow(
                () -> new IllegalArgumentException("Sort direction must be asc or desc")) : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    private ProductPageResponse toPageResponse(Page<Product> page) {
        return new ProductPageResponse(page.getContent().stream().map(mapper::toResponse).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private ProductPageResponse emptyPage(int page, int size) {
        return new ProductPageResponse(List.of(), page, size, 0, 0);
    }

    private List<UUID> parseIds(String ids) {
        if (ids == null || ids.isBlank()) throw new IllegalArgumentException("At least two product IDs are required");
        List<String> values = Arrays.stream(ids.split(",", -1)).map(String::trim).filter(value -> !value.isBlank()).toList();
        if (values.size() < 2 || values.size() > MAX_COMPARISON_PRODUCTS || values.stream().distinct().count() != values.size()) {
            throw new IllegalArgumentException("Comparison requires 2 to 4 unique product IDs");
        }
        try {
            return values.stream().map(UUID::fromString).toList();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Product IDs must be valid UUIDs");
        }
    }

    private ProductCategory category(String value) {
        try {
            return ProductCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Product category is invalid");
        }
    }

    private ProductStatus status(String value) {
        try {
            return ProductStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Product status is invalid");
        }
    }

    private String normalizeCode(String value) { return value.trim().toUpperCase(Locale.ROOT); }
    private String clean(String value) { return value.trim(); }
    private String cleanNullable(String value) { return value == null ? null : value.trim(); }
}