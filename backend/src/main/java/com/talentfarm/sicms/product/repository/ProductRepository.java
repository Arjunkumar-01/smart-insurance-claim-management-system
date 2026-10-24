package com.talentfarm.sicms.product.repository;

import com.talentfarm.sicms.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);
}