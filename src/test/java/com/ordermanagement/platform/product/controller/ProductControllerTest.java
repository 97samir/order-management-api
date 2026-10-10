package com.ordermanagement.platform.product.controller;

import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.product.dto.ProductPageResponse;
import com.ordermanagement.platform.product.dto.ProductRequest;
import com.ordermanagement.platform.product.dto.ProductResponse;
import com.ordermanagement.platform.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProductSuccessfully() throws Exception {

        ProductRequest request = new ProductRequest(
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );

        when(productService.create(any(ProductRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("Laptop profesional"))
                .andExpect(jsonPath("$.price").value(2500.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Electrónica"));

        verify(productService)
                .create(any(ProductRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenProductRequestIsInvalid() throws Exception {

        ProductRequest request = new ProductRequest(
                "",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .create(any(ProductRequest.class));
    }

    @Test
    void shouldReturnConflictWhenProductNameAlreadyExists() throws Exception {

        ProductRequest request = new ProductRequest(
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L
        );

        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Ya existe un producto con el nombre: Laptop"
                ));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un producto con el nombre: Laptop"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productService)
                .create(any(ProductRequest.class));
    }

    @Test
    void shouldFindAllProductsSuccessfully() throws Exception {

        ProductResponse product = new ProductResponse(
                1L,
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );

        ProductPageResponse pageResponse = new ProductPageResponse(
                List.of(product),
                0,
                10,
                1,
                1
        );

        when(productService.findAll(
                isNull(),
                isNull(),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(productService)
                .findAll(
                        isNull(),
                        isNull(),
                        any()
                );
    }

    @Test
    void shouldFindProductsByName() throws Exception {

        ProductResponse product = new ProductResponse(
                1L,
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );

        ProductPageResponse pageResponse = new ProductPageResponse(
                List.of(product),
                0,
                10,
                1,
                1
        );

        when(productService.findAll(
                eq("Laptop"),
                isNull(),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .param("name", "Laptop")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));

        verify(productService)
                .findAll(
                        eq("Laptop"),
                        isNull(),
                        any()
                );
    }

    @Test
    void shouldFindProductsByCategory() throws Exception {

        ProductResponse product = new ProductResponse(
                1L,
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );

        ProductPageResponse pageResponse = new ProductPageResponse(
                List.of(product),
                0,
                10,
                1,
                1
        );

        when(productService.findAll(
                isNull(),
                eq(1L),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].categoryId").value(1))
                .andExpect(jsonPath("$.content[0].categoryName")
                        .value("Electrónica"));

        verify(productService)
                .findAll(
                        isNull(),
                        eq(1L),
                        any()
                );
    }

    @Test
    void shouldFindProductByIdSuccessfully() throws Exception {

        ProductResponse response = new ProductResponse(
                1L,
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );

        when(productService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(2500.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Electrónica"));

        verify(productService)
                .findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        when(productService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Producto no encontrado con id: 999"
                ));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Producto no encontrado con id: 999"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productService)
                .findById(999L);
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {

        ProductRequest request = new ProductRequest(
                "Laptop",
                "Laptop actualizada",
                new BigDecimal("2700.00"),
                15,
                true,
                1L
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Laptop",
                "Laptop actualizada",
                new BigDecimal("2700.00"),
                15,
                true,
                1L,
                "Electrónica"
        );

        when(productService.update(
                eq(1L),
                any(ProductRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("Laptop actualizada"))
                .andExpect(jsonPath("$.price").value(2700.00))
                .andExpect(jsonPath("$.stock").value(15));

        verify(productService)
                .update(eq(1L), any(ProductRequest.class));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {

        doNothing()
                .when(productService)
                .delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productService)
                .delete(1L);
    }

    @Test
    void shouldFindProductsByNameAndCategory() throws Exception {

        ProductResponse product = new ProductResponse(
                1L,
                "Laptop",
                "Laptop profesional",
                new BigDecimal("2500.00"),
                10,
                true,
                1L,
                "Electrónica"
        );

        ProductPageResponse pageResponse = new ProductPageResponse(
                List.of(product),
                0,
                10,
                1,
                1
        );

        when(productService.findAll(
                eq("Laptop"),
                eq(1L),
                any()
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .param("name", "Laptop")
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[0].categoryId").value(1));

        verify(productService)
                .findAll(
                        eq("Laptop"),
                        eq(1L),
                        any()
                );
    }
}