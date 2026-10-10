package com.ordermanagement.platform.product.repository;

import com.ordermanagement.platform.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    boolean existsByNameIgnoreCase(
            String name);

    Page<Product> findByActiveTrue(
            Pageable pageable);

    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<Product> findByActiveTrueAndCategoryId(
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByActiveTrueAndNameContainingIgnoreCaseAndCategoryId(
            String name,
            Long categoryId,
            Pageable pageable
    );
}