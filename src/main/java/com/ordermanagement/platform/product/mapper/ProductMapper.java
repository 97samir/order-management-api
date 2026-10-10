package com.ordermanagement.platform.product.mapper;

import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.product.dto.ProductRequest;
import com.ordermanagement.platform.product.dto.ProductResponse;
import com.ordermanagement.platform.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(
            ProductRequest request,
            Category category) {

        return Product.builder()
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .active(request.active()
                        != null ? request.active()
                        : true)
                .category(category)
                .build();
    }

    public void updateEntity(
            Product product,
            ProductRequest request,
            Category category) {

        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(category);

        if (request.active() != null) {
            product.setActive(request.active());
        }
    }

    public ProductResponse toResponse(
            Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}