package com.ordermanagement.platform.category.controller;

import com.ordermanagement.platform.category.dto.CategoryRequest;
import com.ordermanagement.platform.category.dto.CategoryResponse;
import com.ordermanagement.platform.category.service.CategoryService;
import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {

        CategoryRequest request = new CategoryRequest(
                "Electrónica",
                "Productos electrónicos",
                true
        );

        CategoryResponse response = new CategoryResponse(
                1L,
                "Electrónica",
                "Productos electrónicos",
                true
        );

        when(categoryService.create(any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electrónica"))
                .andExpect(jsonPath("$.description").value("Productos electrónicos"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService)
                .create(any(CategoryRequest.class));
    }

    @Test
    void shouldFindAllCategoriesSuccessfully() throws Exception {

        CategoryResponse first = new CategoryResponse(
                1L,
                "Electrónica",
                "Productos electrónicos",
                true
        );

        CategoryResponse second = new CategoryResponse(
                2L,
                "Hogar",
                "Productos para el hogar",
                true
        );

        when(categoryService.findAll())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electrónica"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Hogar"));

        verify(categoryService)
                .findAll();
    }

    @Test
    void shouldFindCategoryByIdSuccessfully() throws Exception {

        CategoryResponse response = new CategoryResponse(
                1L,
                "Electrónica",
                "Productos electrónicos",
                true
        );

        when(categoryService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electrónica"))
                .andExpect(jsonPath("$.description").value("Productos electrónicos"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService)
                .findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {

        when(categoryService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Categoría no encontrada con id: 999"
                ));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Categoría no encontrada con id: 999"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(categoryService)
                .findById(999L);
    }

    @Test
    void shouldUpdateCategorySuccessfully() throws Exception {

        CategoryRequest request = new CategoryRequest(
                "Electrónica",
                "Productos electrónicos actualizados",
                true
        );

        CategoryResponse response = new CategoryResponse(
                1L,
                "Electrónica",
                "Productos electrónicos actualizados",
                true
        );

        when(categoryService.update(eq(1L), any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electrónica"))
                .andExpect(jsonPath("$.description")
                        .value("Productos electrónicos actualizados"))
                .andExpect(jsonPath("$.active").value(true));

        verify(categoryService)
                .update(eq(1L), any(CategoryRequest.class));
    }

    @Test
    void shouldReturnConflictWhenCategoryNameAlreadyExists() throws Exception {

        CategoryRequest request = new CategoryRequest(
                "Electrónica",
                "Productos electrónicos",
                true
        );

        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Ya existe una categoría con el nombre: Electrónica"
                ));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ya existe una categoría con el nombre: Electrónica"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(categoryService)
                .create(any(CategoryRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenCategoryRequestIsInvalid() throws Exception {

        CategoryRequest request = new CategoryRequest(
                "",
                "Productos electrónicos",
                true
        );

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never())
                .create(any(CategoryRequest.class));
    }

    @Test
    void shouldDeleteCategorySuccessfully() throws Exception {

        doNothing()
                .when(categoryService)
                .delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService)
                .delete(1L);
    }
}