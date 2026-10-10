package com.ordermanagement.platform.order.repository;

import com.ordermanagement.platform.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(
            Long customerId,
            Pageable pageable
    );
}