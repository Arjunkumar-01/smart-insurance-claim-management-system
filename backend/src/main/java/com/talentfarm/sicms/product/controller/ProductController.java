package com.talentfarm.sicms.product.controller;

import com.talentfarm.sicms.product.dto.request.CreateProductRequest;
import com.talentfarm.sicms.product.dto.request.UpdateProductRequest;
import com.talentfarm.sicms.product.dto.response.ProductComparisonResponse;
import com.talentfarm.sicms.product.dto.response.ProductPageResponse;
import com.talentfarm.sicms.product.dto.response.ProductResponse;
import com.talentfarm.sicms.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ProductResponse update(@PathVariable UUID productId, @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(productId, request);
    }

    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMINISTRATOR')")
    public ProductComparisonResponse compare(Authentication authentication, @RequestParam String ids) {
        return productService.compare(authentication, ids);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMINISTRATOR')")
    public ProductPageResponse list(Authentication authentication,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String productCode,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return productService.list(authentication, name, productCode, category, status, page, size, sort);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMINISTRATOR')")
    public ProductResponse get(@PathVariable UUID productId, Authentication authentication) {
        return productService.get(productId, authentication);
    }
}