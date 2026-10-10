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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;
    private ProductRequest request;
    private ProductResponse response;

    @BeforeEach
    void setUp() {

        category = Category.builder()
                .id(1L)
                .name("Electrónica")
                .description("Productos electrónicos")
                .active(true)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Laptop para trabajo")
                .price(new BigDecimal("2500.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build();

        request = new ProductRequest(
                "Laptop",
                "Laptop para trabajo",
                new BigDecimal("2500.00"),
                10,
                true,
                1L
        );

        response = new ProductResponse(
                1L,
                "Laptop",
                "Laptop para trabajo",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );
    }

    @Test
    void shouldCreateProductSuccessfully() {

        when(productRepository.existsByNameIgnoreCase(request.name()))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productMapper.toEntity(request, category))
                .thenReturn(product);

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Laptop", result.name());
        assertEquals(new BigDecimal("2500.00"), result.price());
        assertEquals(10, result.stock());
        assertEquals(1L, result.categoryId());

        verify(productRepository)
                .existsByNameIgnoreCase(request.name());

        verify(categoryRepository)
                .findById(1L);

        verify(productRepository)
                .save(product);
    }

    @Test
    void shouldThrowExceptionWhenProductNameAlreadyExists() {

        when(productRepository.existsByNameIgnoreCase(request.name()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> productService.create(request)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {

        when(productRepository.existsByNameIgnoreCase(request.name()))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.create(request)
        );

        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toEntity(any(ProductRequest.class), any(Category.class));
    }

    @Test
    void shouldFindProductByIdSuccessfully() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Laptop", result.name());

        verify(productRepository).findById(1L);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.findById(99L)
        );

        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void shouldFindAllActiveProductsWithoutFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.findByActiveTrue(pageable))
                .thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductPageResponse result = productService.findAll(
                null,
                null,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(0, result.page());
        assertEquals(10, result.size());

        verify(productRepository)
                .findByActiveTrue(pageable);

        verify(productRepository, never())
                .findAll(any(Pageable.class));
    }

    @Test
    void shouldFindProductsByNameAndCategory() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository
                .findByActiveTrueAndNameContainingIgnoreCaseAndCategoryId(
                        "Laptop",
                        1L,
                        pageable
                ))
                .thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductPageResponse result = productService.findAll(
                "Laptop",
                1L,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Laptop", result.content().getFirst().name());
        assertEquals(1L, result.content().getFirst().categoryId());

        verify(productRepository)
                .findByActiveTrueAndNameContainingIgnoreCaseAndCategoryId(
                        "Laptop",
                        1L,
                        pageable
                );

        verify(productRepository, never())
                .findByActiveTrueAndNameContainingIgnoreCase(
                        anyString(),
                        any(Pageable.class)
                );

        verify(productRepository, never())
                .findByActiveTrueAndCategoryId(
                        anyLong(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldTrimNameWhenSearchingProducts() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository
                .findByActiveTrueAndNameContainingIgnoreCase(
                        "Laptop",
                        pageable
                ))
                .thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductPageResponse result = productService.findAll(
                "  Laptop  ",
                null,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());

        verify(productRepository)
                .findByActiveTrueAndNameContainingIgnoreCase(
                        "Laptop",
                        pageable
                );
    }

    @Test
    void shouldFindProductsByName() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.findByActiveTrueAndNameContainingIgnoreCase(
                "Laptop",
                pageable
        )).thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductPageResponse result = productService.findAll(
                "Laptop",
                null,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Laptop", result.content().getFirst().name());

        verify(productRepository)
                .findByActiveTrueAndNameContainingIgnoreCase("Laptop", pageable);

        verify(productRepository, never())
                .findByActiveTrueAndCategoryId(anyLong(), any(Pageable.class));
    }

    @Test
    void shouldFindProductsByCategory() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                pageable,
                1
        );

        when(productRepository.findByActiveTrueAndCategoryId(1L, pageable))
                .thenReturn(productPage);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductPageResponse result = productService.findAll(
                null,
                1L,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.content().getFirst().categoryId());

        verify(productRepository)
                .findByActiveTrueAndCategoryId(1L, pageable);

        verify(productRepository, never())
                .findByActiveTrueAndNameContainingIgnoreCase(
                        anyString(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldUpdateProductSuccessfully() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = productService.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Laptop", result.name());

        verify(productMapper)
                .updateEntity(product, request, category);

        verify(productRepository)
                .save(product);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentProduct() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.update(99L, request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingProductName() {

        Product anotherProduct = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(new BigDecimal("200.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build();

        ProductRequest updateRequest = new ProductRequest(
                "Mouse",
                "Laptop para trabajo",
                new BigDecimal("2500.00"),
                10,
                true,
                1L
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.existsByNameIgnoreCase("Mouse"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> productService.update(1L, updateRequest)
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldDeactivateProductSuccessfully() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.delete(1L);

        assertFalse(product.getActive());

        verify(productRepository).save(product);

        verify(productRepository, never())
                .delete(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentProduct() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.delete(99L)
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }

    //producto ya desactivado
    @Test
    void shouldThrowExceptionWhenDeletingAlreadyInactiveProduct() {

        product.setActive(false);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalStateException.class,
                () -> productService.delete(1L)
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }

    // actualizacion de categoria inexistente
    @Test
    void shouldThrowExceptionWhenUpdatingWithNonexistentCategory() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        ProductRequest updateRequest = new ProductRequest(
                "Laptop",
                "Laptop actualizada",
                new BigDecimal("2700.00"),
                15,
                true,
                99L
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.update(1L, updateRequest)
        );

        verify(productRepository, never())
                .save(any(Product.class));

        verify(productMapper, never())
                .updateEntity(
                        any(Product.class),
                        any(ProductRequest.class),
                        any(Category.class)
                );
    }

    @Test
    void shouldTrimProductNameWhenCreating() {

        ProductRequest requestWithSpaces = new ProductRequest(
                "  Laptop  ",
                "Laptop para trabajo",
                new BigDecimal("2500.00"),
                10,
                true,
                1L
        );

        when(productRepository.existsByNameIgnoreCase("Laptop"))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productMapper.toEntity(
                requestWithSpaces,
                category
        )).thenReturn(product);

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = productService.create(requestWithSpaces);

        assertNotNull(result);

        verify(productRepository)
                .existsByNameIgnoreCase("Laptop");
    }
}