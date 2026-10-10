package com.ordermanagement.platform.customer.service;

import com.ordermanagement.platform.auth.repository.UserRepository;
import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.customer.dto.CustomerRequest;
import com.ordermanagement.platform.customer.dto.CustomerResponse;
import com.ordermanagement.platform.customer.entity.Customer;
import com.ordermanagement.platform.customer.mapper.CustomerMapper;
import com.ordermanagement.platform.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;

    public CustomerResponse create(
            CustomerRequest request) {

        String email = request.email().trim().toLowerCase();

        if(!userRepository.existsByEmailIgnoreCase(email)){
            throw new ResourceNotFoundException(
                    "No existe un usuario registrado con el email: " + email
            );
        }

        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                    "Ya existe un cliente con el email: " + request.email()
            );
        }

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    public Page<CustomerResponse> findAll(
            String search,
            Pageable pageable) {

        Page<Customer> customerPage;

        if (search != null && !search.isBlank()) {

            customerPage = customerRepository
                    .findByActiveTrueAndFirstNameContainingIgnoreCaseOrActiveTrueAndLastNameContainingIgnoreCase(
                            search,
                            search,
                            pageable
                    );

        } else {

            customerPage = customerRepository.findByActiveTrue(pageable);
        }

        return customerPage.map(customerMapper::toResponse);
    }

    public CustomerResponse findById(
            Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id
                ));

        return customerMapper.toResponse(customer);
    }

    public CustomerResponse update(
            Long id,
            CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id
                ));

        /*
        if (!customer.getEmail().equalsIgnoreCase(request.email())
                && customerRepository.existsByEmailIgnoreCase(request.email())) {

            throw new DuplicateResourceException(
                    "Ya existe un cliente con el email: " + request.email()
            );
        }
        */

        customerMapper.updateEntity(customer, request);
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(updatedCustomer);
    }

    public void delete(
            Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id
                ));

        if (!customer.getActive()) {
            throw new IllegalStateException(
                    "El cliente ya se encuentra desactivado"
            );
        }

        customer.setActive(false);

        customerRepository.save(customer);
    }
}