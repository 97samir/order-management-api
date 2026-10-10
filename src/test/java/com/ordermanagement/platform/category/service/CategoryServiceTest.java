package com.ordermanagement.platform.category.service;

import com.ordermanagement.platform.category.dto.CategoryRequest;
import com.ordermanagement.platform.category.dto.CategoryResponse;
import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.category.mapper.CategoryMapper;
import com.ordermanagement.platform.category.repository.CategoryRepository;
import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {

        category = Category.builder()
                .id(1L)
                .name("Electrónica")
                .description("Productos electrónicos")
                .active(true)
                .build();

        request = new CategoryRequest(
                "Electrónica",
                "Productos electrónicos",
                true
        );

        response = new CategoryResponse(
                1L,
                "Electrónica",
                "Productos electrónicos",
                true
        );
    }

    @Test
    void shouldCreateCategorySuccessfully() {

        when(categoryRepository.existsByNameIgnoreCase(request.name()))
                .thenReturn(false);

        when(categoryMapper.toEntity(request))
                .thenReturn(category);

        when(categoryRepository.save(category))
                .thenReturn(category);

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electrónica", result.name());
        assertEquals("Productos electrónicos", result.description());
        assertTrue(result.active());

        verify(categoryRepository)
                .existsByNameIgnoreCase(request.name());

        verify(categoryMapper)
                .toEntity(request);

        verify(categoryRepository)
                .save(category);

        verify(categoryMapper)
                .toResponse(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameAlreadyExists() {

        when(categoryRepository.existsByNameIgnoreCase(request.name()))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.create(request)
        );

        assertEquals(
                "Ya existe una categoría con el nombre: Electrónica",
                exception.getMessage()
        );

        verify(categoryRepository)
                .existsByNameIgnoreCase(request.name());

        verify(categoryMapper, never())
                .toEntity(any());

        verify(categoryRepository, never())
                .save(any());
    }

    @Test
    void shouldFindAllCategoriesSuccessfully() {

        Category secondCategory = Category.builder()
                .id(2L)
                .name("Hogar")
                .description("Productos para el hogar")
                .active(true)
                .build();

        CategoryResponse secondResponse = new CategoryResponse(
                2L,
                "Hogar",
                "Productos para el hogar",
                true
        );

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, secondCategory));

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        when(categoryMapper.toResponse(secondCategory))
                .thenReturn(secondResponse);

        List<CategoryResponse> result = categoryService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("Electrónica", result.get(0).name());
        assertEquals("Hogar", result.get(1).name());

        verify(categoryRepository)
                .findAll();

        verify(categoryMapper)
                .toResponse(category);

        verify(categoryMapper)
                .toResponse(secondCategory);
    }

    @Test
    void shouldFindCategoryByIdSuccessfully() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        CategoryResponse result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electrónica", result.name());

        verify(categoryRepository)
                .findById(1L);

        verify(categoryMapper)
                .toResponse(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.findById(1L)
        );

        assertEquals(
                "Categoría no encontrada con id: 1",
                exception.getMessage()
        );

        verify(categoryRepository)
                .findById(1L);

        verify(categoryMapper, never())
                .toResponse(any());
    }

    @Test
    void shouldUpdateCategorySuccessfully() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(category))
                .thenReturn(category);

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        CategoryResponse result = categoryService.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electrónica", result.name());
        assertEquals("Productos electrónicos", result.description());

        verify(categoryRepository)
                .findById(1L);

        verify(categoryMapper)
                .updateEntity(category, request);

        verify(categoryRepository)
                .save(category);

        verify(categoryMapper)
                .toResponse(category);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingCategoryName() {

        CategoryRequest updateRequest = new CategoryRequest(
                "Hogar",
                "Productos electrónicos",
                true
        );

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameIgnoreCase("Hogar"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.update(1L, updateRequest)
        );

        assertEquals(
                "Ya existe una categoría con el nombre: Hogar",
                exception.getMessage()
        );

        verify(categoryRepository)
                .findById(1L);

        verify(categoryRepository)
                .existsByNameIgnoreCase("Hogar");

        verify(categoryMapper, never())
                .updateEntity(any(), any());

        verify(categoryRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteCategorySuccessfully() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository)
                .findById(1L);

        verify(categoryRepository)
                .delete(category);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentCategory() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.delete(1L)
        );

        assertEquals(
                "Categoría no encontrada con id: 1",
                exception.getMessage()
        );

        verify(categoryRepository)
                .findById(1L);

        verify(categoryRepository, never())
                .delete(any());
    }
}