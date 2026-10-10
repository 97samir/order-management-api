package com.ordermanagement.platform.category.mapper;

import com.ordermanagement.platform.category.dto.CategoryRequest;
import com.ordermanagement.platform.category.dto.CategoryResponse;
import com.ordermanagement.platform.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    // convierte request a entity
    public Category toEntity(
            CategoryRequest request){

        return Category.builder()
                .name(request.name())
                .description(request.description())
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    // actualizar
    public void updateEntity(
            Category category,
            CategoryRequest request) {

        category.setName(request.name());
        category.setDescription(request.description());

        if (request.active() != null) {
            category.setActive(request.active());
        }
    }

    // convierte entity a response
    public CategoryResponse toResponse(
            Category category){

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }


}
