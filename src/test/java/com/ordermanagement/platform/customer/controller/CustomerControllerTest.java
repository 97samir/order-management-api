package com.ordermanagement.platform.customer.controller;

import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.customer.dto.CustomerRequest;
import com.ordermanagement.platform.customer.dto.CustomerResponse;
import com.ordermanagement.platform.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void shouldCreateCustomerSuccessfully() throws Exception {

        CustomerRequest request = new CustomerRequest(
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        CustomerResponse response = new CustomerResponse(
                1L,
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        when(customerService.create(any(CustomerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.phone").value("+51987654321"))
                .andExpect(jsonPath("$.active").value(true));

        verify(customerService)
                .create(any(CustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenCustomerRequestIsInvalid() throws Exception {

        CustomerRequest request = new CustomerRequest(
                "",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(customerService, never())
                .create(any(CustomerRequest.class));
    }

    @Test
    void shouldReturnConflictWhenCustomerEmailAlreadyExists() throws Exception {

        CustomerRequest request = new CustomerRequest(
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        when(customerService.create(any(CustomerRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Ya existe un cliente con el email: juan@example.com"
                ));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ya existe un cliente con el email: juan@example.com"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(customerService)
                .create(any(CustomerRequest.class));
    }

    @Test
    void shouldFindAllCustomersSuccessfully() throws Exception {

        CustomerResponse customer = new CustomerResponse(
                1L,
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        PageImpl<CustomerResponse> page = new PageImpl<>(
                List.of(customer),
                PageRequest.of(0, 10),
                1
        );

        when(customerService.findAll(
                isNull(),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Juan"))
                .andExpect(jsonPath("$.content[0].lastName").value("Pérez"))
                .andExpect(jsonPath("$.content[0].email").value("juan@example.com"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(customerService)
                .findAll(isNull(), any());
    }

    @Test
    void shouldFindCustomersBySearch() throws Exception {

        CustomerResponse customer = new CustomerResponse(
                1L,
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        PageImpl<CustomerResponse> page = new PageImpl<>(
                List.of(customer),
                PageRequest.of(0, 10),
                1
        );

        when(customerService.findAll(
                eq("Juan"),
                any()
        )).thenReturn(page);

        mockMvc.perform(get("/api/customers")
                        .param("search", "Juan")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Juan"))
                .andExpect(jsonPath("$.content[0].lastName").value("Pérez"));

        verify(customerService)
                .findAll(eq("Juan"), any());
    }

    @Test
    void shouldFindCustomerByIdSuccessfully() throws Exception {

        CustomerResponse response = new CustomerResponse(
                1L,
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        when(customerService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.phone").value("+51987654321"))
                .andExpect(jsonPath("$.active").value(true));

        verify(customerService)
                .findById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {

        when(customerService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Cliente no encontrado con id: 999"
                ));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Cliente no encontrado con id: 999"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(customerService)
                .findById(999L);
    }

    @Test
    void shouldUpdateCustomerSuccessfully() throws Exception {

        CustomerRequest request = new CustomerRequest(
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51999999999",
                true
        );

        CustomerResponse response = new CustomerResponse(
                1L,
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51999999999",
                true
        );

        when(customerService.update(
                eq(1L),
                any(CustomerRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.phone").value("+51999999999"));

        verify(customerService)
                .update(eq(1L), any(CustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidData() throws Exception {

        CustomerRequest request = new CustomerRequest(
                "",
                "",
                "email-invalido",
                "123",
                true
        );

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(customerService, never())
                .update(anyLong(), any(CustomerRequest.class));
    }

    @Test
    void shouldDeleteCustomerSuccessfully() throws Exception {

        doNothing()
                .when(customerService)
                .delete(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(customerService)
                .delete(1L);
    }
}