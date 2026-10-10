package com.ordermanagement.platform.order.controller;

import com.ordermanagement.platform.common.exception.InvalidOrderRequestException;
import com.ordermanagement.platform.common.exception.InvalidOrderStateException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.order.dto.OrderItemRequest;
import com.ordermanagement.platform.order.dto.OrderItemResponse;
import com.ordermanagement.platform.order.dto.OrderRequest;
import com.ordermanagement.platform.order.dto.OrderResponse;
import com.ordermanagement.platform.order.enums.OrderStatus;
import com.ordermanagement.platform.order.service.OrderService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;


    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(2L, 1)
                )
        );

        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("55.00"),
                OrderStatus.PENDING,
                LocalDateTime.of(2026, 9, 13, 10, 30),
                List.of(
                        new OrderItemResponse(
                                1L,
                                1L,
                                "Laptop",
                                2,
                                new BigDecimal("20.00"),
                                new BigDecimal("40.00")
                        ),
                        new OrderItemResponse(
                                2L,
                                2L,
                                "Mouse",
                                1,
                                new BigDecimal("15.00"),
                                new BigDecimal("15.00")
                        )
                )
        );

        when(orderService.create(any(OrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("Juan Pérez"))
                .andExpect(jsonPath("$.total").value(55.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items.length()").value(2));

        verify(orderService).create(any(OrderRequest.class));
    }


    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {

        OrderRequest request = new OrderRequest(
                null,
                List.of()
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).create(any(OrderRequest.class));
    }


    @Test
    void shouldReturnBadRequestWhenOrderContainsDuplicateProduct() throws Exception {

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(1L, 3)
                )
        );

        when(orderService.create(any(OrderRequest.class)))
                .thenThrow(new InvalidOrderRequestException(
                        "El producto con id 1 no puede aparecer más de una vez en el pedido"
                ));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("El producto con id 1 no puede aparecer más de una vez en el pedido"));

        verify(orderService).create(any(OrderRequest.class));
    }


    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        when(orderService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Pedido no encontrado con id: 999"
                ));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Pedido no encontrado con id: 999"));

        verify(orderService).findById(999L);
    }


    @Test
    void shouldFindAllOrdersSuccessfully() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("55.00"),
                OrderStatus.PENDING,
                LocalDateTime.of(2026, 9, 13, 10, 30),
                List.of()
        );

        PageImpl<OrderResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10),
                1
        );

        when(orderService.findAll(isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].customerId").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(orderService).findAll(isNull(), any());
    }


    @Test
    void shouldFindOrdersByCustomerSuccessfully() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                5L,
                "María López",
                new BigDecimal("100.00"),
                OrderStatus.CONFIRMED,
                LocalDateTime.of(2026, 9, 13, 11, 0),
                List.of()
        );

        PageImpl<OrderResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10),
                1
        );

        when(orderService.findAll(eq(5L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders")
                        .param("customerId", "5")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].customerId").value(5))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(orderService).findAll(eq(5L), any());
    }


    @Test
    void shouldConfirmOrderSuccessfully() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("55.00"),
                OrderStatus.CONFIRMED,
                LocalDateTime.of(2026, 9, 13, 10, 30),
                List.of()
        );

        when(orderService.confirm(1L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).confirm(1L);
    }


    @Test
    void shouldCancelOrderSuccessfully() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("55.00"),
                OrderStatus.CANCELLED,
                LocalDateTime.of(2026, 9, 13, 10, 30),
                List.of()
        );

        when(orderService.cancel(1L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancel(1L);
    }


    @Test
    void shouldCompleteOrderSuccessfully() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("55.00"),
                OrderStatus.COMPLETED,
                LocalDateTime.of(2026, 9, 13, 10, 30),
                List.of()
        );

        when(orderService.complete(1L))
                .thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(orderService).complete(1L);
    }


    @Test
    void shouldReturnConflictWhenOrderStateIsInvalid() throws Exception {

        when(orderService.confirm(1L))
                .thenThrow(new InvalidOrderStateException(
                        "Solo se pueden confirmar pedidos en estado PENDING"
                ));

        mockMvc.perform(patch("/api/orders/1/confirm"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Solo se pueden confirmar pedidos en estado PENDING"));

        verify(orderService).confirm(1L);
    }
}