package com.ordermanagement.platform.customer.mapper;

import com.ordermanagement.platform.customer.dto.CustomerRequest;
import com.ordermanagement.platform.customer.dto.CustomerResponse;
import com.ordermanagement.platform.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {

        return Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email().trim().toLowerCase())
                .phone(request.phone())
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    // el email no se modifica por que tiene vinculo con logico
    // con user y customer
    public void updateEntity(
            Customer customer,
            CustomerRequest request
    ) {

        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        //customer.setEmail(request.email());
        customer.setPhone(request.phone());

        if (request.active() != null) {
            customer.setActive(request.active());
        }
    }

    public CustomerResponse toResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getActive()
        );
    }
}