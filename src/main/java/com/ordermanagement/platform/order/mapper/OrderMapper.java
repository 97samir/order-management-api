package com.ordermanagement.platform.order.mapper;

import com.ordermanagement.platform.customer.entity.Customer;
import com.ordermanagement.platform.order.dto.OrderItemResponse;
import com.ordermanagement.platform.order.dto.OrderResponse;
import com.ordermanagement.platform.order.entity.Order;
import com.ordermanagement.platform.orderItem.entity.OrderItem;
import com.ordermanagement.platform.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {

        Product product = item.getProduct();

        return new OrderItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    public OrderResponse toResponse(Order order) {

        Customer customer = order.getCustomer();

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }
}