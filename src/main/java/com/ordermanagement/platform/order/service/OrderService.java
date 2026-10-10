package com.ordermanagement.platform.order.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    /* transactional todo en una sola funcion, si no se revierte */
    @Transactional
    public OrderResponse create(
            OrderRequest request) {

        // Set no permite elementos duplicados.
        Set<Long> productIds = new HashSet<>();

        for (OrderItemRequest itemRequest : request.items()) {

            if (!productIds.add(itemRequest.productId())) {
                throw new InvalidOrderRequestException(
                        "El producto con id " + itemRequest.productId()
                                + " no puede aparecer más de una vez en el pedido"
                );
            }
        }

        Customer customer = customerRepository
                .findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + request.customerId()
                ));

        validateCustomerAccess(customer);

        if (!customer.getActive()) {
            throw new IllegalStateException(
                    "No se puede crear un pedido para un cliente inactivo"
            );
        }

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: "
                                    + itemRequest.productId()
                    ));

            if (!product.getActive()) {
                throw new IllegalStateException(
                        "El producto no está disponible: " + product.getName()
                );
            }

            if (product.getStock() < itemRequest.quantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto: "
                                + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.quantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);

            // funcion para descuento de stock
            product.setStock(
                    product.getStock() - itemRequest.quantity()
            );

            total = total.add(subtotal);
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + id
                ));

        if (!isAdmin()) {

            Customer authenticatedCustomer =
                    getAuthenticatedCustomer();

            if (!order.getCustomer().getId()
                    .equals(authenticatedCustomer.getId())) {

                throw new AccessDeniedException(
                        "No tienes permisos para acceder a este pedido"
                );
            }
        }

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(
            Long customerId,
            Pageable pageable) {

        Page<Order> orderPage;

        if (isAdmin()) {

            if (customerId != null) {
                orderPage = orderRepository.findByCustomerId(
                        customerId,
                        pageable
                );
            } else {
                orderPage = orderRepository.findAll(pageable);
            }

        } else {

            Customer authenticatedCustomer =
                    getAuthenticatedCustomer();

            orderPage = orderRepository.findByCustomerId(
                    authenticatedCustomer.getId(),
                    pageable
            );
        }

        return orderPage.map(orderMapper::toResponse);
    }

    //  EXCEPCION PEDIDOS
    @Transactional
    public OrderResponse confirm(
            Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + id
                ));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Solo se puede confirmar un pedido en estado PENDING"
            );
        }

        order.setStatus(OrderStatus.CONFIRMED);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(
            Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + id
                ));

        // proteccion de propiertario, si el pedido corresponde al customer
        if (!isAdmin()) {

            Customer authenticatedCustomer =
                    getAuthenticatedCustomer();

            if (!order.getCustomer().getId()
                    .equals(authenticatedCustomer.getId())) {

                throw new AccessDeniedException(
                        "No tienes permisos para cancelar este pedido"
                );
            }
        }

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new InvalidOrderStateException(
                    "No se puede cancelar un pedido con estado "
                            + order.getStatus()
            );
        }

        // si se cancela el pedido, regresa a su stock anterior
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse complete(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + id
                ));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "Solo se puede completar un pedido en estado CONFIRMED"
            );
        }

        order.setStatus(OrderStatus.COMPLETED);

        return orderMapper.toResponse(order);
    }

    // obetener el email mediante spring security
    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }

    // obtener el Customer autenticado
    private Customer getAuthenticatedCustomer() {

        String email = getAuthenticatedUserEmail();

        return customerRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un cliente asociado al usuario autenticado"
                ));
    }

    private boolean isAdmin() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );
    }

    private void validateCustomerAccess(Customer customer) {

        if (isAdmin()) {
            return;
        }

        Customer authenticatedCustomer = getAuthenticatedCustomer();

        if (!authenticatedCustomer.getId().equals(customer.getId())) {
            throw new AccessDeniedException(
                    "No tienes permisos para acceder a este cliente"
            );
        }
    }

}