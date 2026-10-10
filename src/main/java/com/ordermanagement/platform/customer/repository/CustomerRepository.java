package com.ordermanagement.platform.customer.repository;

import com.ordermanagement.platform.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    Page<Customer> findByActiveTrue(Pageable pageable);

    Page<Customer> findByActiveTrueAndFirstNameContainingIgnoreCaseOrActiveTrueAndLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );
}