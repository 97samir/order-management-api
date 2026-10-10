package com.ordermanagement.platform.order.service;

import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.common.exception.InsufficientStockException;
import com.ordermanagement.platform.common.exception.InvalidOrderRequestException;
import com.ordermanagement.platform.common.exception.InvalidOrderStateException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.customer.entity.Customer;
import com.ordermanagement.platform.customer.repository.CustomerRepository;
import com.ordermanagement.platform.order.dto.OrderItemRequest;
import com.ordermanagement.platform.order.dto.OrderRequest;
import com.ordermanagement.platform.order.dto.OrderResponse;
import com.ordermanagement.platform.order.entity.Order;
import com.ordermanagement.platform.order.enums.OrderStatus;
import com.ordermanagement.platform.order.mapper.OrderMapper;
import com.ordermanagement.platform.order.repository.OrderRepository;
import com.ordermanagement.platform.orderItem.entity.OrderItem;
import com.ordermanagement.platform.product.entity.Product;
import com.ordermanagement.platform.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {

        customer = Customer.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .active(true)
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Electrónica")
                .active(true)
                .build();

        laptop = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("2500.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build();

        mouse = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(new BigDecimal("200.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() {

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(2L, 1)
                )
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(laptop));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(mouse));

        OrderResponse expectedResponse = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("5200.00"),
                OrderStatus.PENDING,
                null,
                List.of()
        );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(expectedResponse);

        OrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("5200.00"), response.total());
        assertEquals(OrderStatus.PENDING, response.status());

        assertEquals(8, laptop.getStock());
        assertEquals(9, mouse.getStock());

        verify(orderRepository).save(any(Order.class));
        verify(productRepository).findById(1L);
        verify(productRepository).findById(2L);
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        OrderRequest request = new OrderRequest(
                99L,
                List.of(
                        new OrderItemRequest(1L, 1)
                )
        );

        when(customerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.create(request)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerIsInactive() {

        customer.setActive(false);

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 1)
                )
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.create(request)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        laptop.setStock(1);

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 5)
                )
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(laptop));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.create(request)
        );

        assertEquals(1, laptop.getStock());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenProductIsInactive() {

        laptop.setActive(false);

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 1)
                )
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(laptop));

        assertThrows(
                IllegalStateException.class,
                () -> orderService.create(request)
        );

        assertEquals(10, laptop.getStock());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    // producto duplicado
    @Test
    void shouldThrowExceptionWhenProductIsDuplicated() {

        OrderRequest request = new OrderRequest(
                1L,
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(1L, 3)
                )
        );

        assertThrows(
                InvalidOrderRequestException.class,
                () -> orderService.create(request)
        );

        verify(customerRepository, never()).findById(anyLong());
        verify(productRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    // confirmar
    @Test
    void shouldConfirmPendingOrder() {

        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("5200.00"))
                .build();

        OrderResponse expectedResponse = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("5200.00"),
                OrderStatus.CONFIRMED,
                null,
                List.of()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(expectedResponse);

        OrderResponse response = orderService.confirm(1L);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(OrderStatus.CONFIRMED, response.status());
    }

    // transicion invalida
    @Test
    void shouldNotConfirmCompletedOrder() {

        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.COMPLETED)
                .total(new BigDecimal("5200.00"))
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.confirm(1L)
        );

        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        verify(orderMapper, never()).toResponse(any(Order.class));
    }

    // cancelar, devolver stock
    @Test
    void shouldCancelOrderAndRestoreStock() {

        OrderItem laptopItem = OrderItem.builder()
                .id(1L)
                .product(laptop)
                .quantity(3)
                .unitPrice(new BigDecimal("2500.00"))
                .subtotal(new BigDecimal("7500.00"))
                .build();

        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("7500.00"))
                .items(List.of(laptopItem))
                .build();

        laptop.setStock(7);

        OrderResponse expectedResponse = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("7500.00"),
                OrderStatus.CANCELLED,
                null,
                List.of()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(expectedResponse);

        OrderResponse response = orderService.cancel(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, laptop.getStock());
        assertEquals(OrderStatus.CANCELLED, response.status());
    }

    // no cancelar pedido completado
    @Test
    void shouldNotCancelCompletedOrder() {

        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.COMPLETED)
                .total(new BigDecimal("5200.00"))
                .items(List.of())
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.cancel(1L)
        );

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    // completyar correctamente
    @Test
    void shouldCompleteConfirmedOrder() {

        Order order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.CONFIRMED)
                .total(new BigDecimal("5200.00"))
                .items(List.of())
                .build();

        OrderResponse expectedResponse = new OrderResponse(
                1L,
                1L,
                "Juan Pérez",
                new BigDecimal("5200.00"),
                OrderStatus.COMPLETED,
                null,
                List.of()
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(expectedResponse);

        OrderResponse response = orderService.complete(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(OrderStatus.COMPLETED, response.status());
    }
}