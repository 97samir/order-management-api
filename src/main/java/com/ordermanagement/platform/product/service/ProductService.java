package com.ordermanagement.platform.product.service;

import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.category.repository.CategoryRepository;
import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.product.dto.ProductPageResponse;
import com.ordermanagement.platform.product.dto.ProductRequest;
import com.ordermanagement.platform.product.dto.ProductResponse;
import com.ordermanagement.platform.product.entity.Product;
import com.ordermanagement.platform.product.mapper.ProductMapper;
import com.ordermanagement.platform.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(
            ProductRequest request) {

        String name = request.name().trim();

        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(
                    "Ya existe un producto con el nombre: " + name
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + request.categoryId()
                ));

        Product product = productMapper.toEntity(request, category);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    public ProductPageResponse findAll(
            String name,
            Long categoryId,
            Pageable pageable) {

        Page<Product> productPage;

        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = categoryId != null;

        if (hasName && hasCategory) {

            productPage = productRepository
                    .findByActiveTrueAndNameContainingIgnoreCaseAndCategoryId(
                            name.trim(),
                            categoryId,
                            pageable
                    );

        } else if (hasName) {

            productPage = productRepository
                    .findByActiveTrueAndNameContainingIgnoreCase(
                            name.trim(),
                            pageable
                    );

        } else if (hasCategory) {

            productPage = productRepository
                    .findByActiveTrueAndCategoryId(
                            categoryId,
                            pageable
                    );

        } else {

            productPage = productRepository
                    .findByActiveTrue(pageable);
        }

        List<ProductResponse> products = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return new ProductPageResponse(
                products,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    public ProductResponse findById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id
                ));

        return productMapper.toResponse(product);
    }

    public ProductResponse update(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id
                ));

        String name = request.name().trim();

        if (!product.getName().equalsIgnoreCase(name)
                && productRepository.existsByNameIgnoreCase(name)) {

            throw new DuplicateResourceException(
                    "Ya existe un producto con el nombre: " + name
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + request.categoryId()
                ));

        productMapper.updateEntity(product, request, category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public void delete(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id
                ));

        if (!product.getActive()) {
            throw new IllegalStateException(
                    "El producto ya se encuentra desactivado"
            );
        }

        product.setActive(false);
        productRepository.save(product);
    }
}