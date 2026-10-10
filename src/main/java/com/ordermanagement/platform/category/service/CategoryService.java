package com.ordermanagement.platform.category.service;

import com.ordermanagement.platform.category.dto.CategoryRequest;
import com.ordermanagement.platform.category.dto.CategoryResponse;
import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.category.mapper.CategoryMapper;
import com.ordermanagement.platform.category.repository.CategoryRepository;
import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse create(
            CategoryRequest request) {

        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría con el nombre: " + request.name()
            );
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }
    public CategoryResponse findById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + id
                ));

        return categoryMapper.toResponse(category);
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse update(
            Long id,
            CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + id
                ));

        if (!category.getName().equalsIgnoreCase(request.name())
                && categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría con el nombre: " + request.name()
            );
        }

        categoryMapper.updateEntity(category, request);
        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }

    public void delete(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + id
                ));

        categoryRepository.delete(category);
    }
}