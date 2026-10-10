package com.ordermanagement.platform.customer.service;

import com.ordermanagement.platform.common.exception.DuplicateResourceException;
import com.ordermanagement.platform.common.exception.ResourceNotFoundException;
import com.ordermanagement.platform.customer.dto.CustomerRequest;
import com.ordermanagement.platform.customer.dto.CustomerResponse;
import com.ordermanagement.platform.customer.entity.Customer;
import com.ordermanagement.platform.customer.mapper.CustomerMapper;
import com.ordermanagement.platform.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest request;
    private CustomerResponse response;

    @BeforeEach
    void setUp() {

        customer = Customer.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phone("+51987654321")
                .active(true)
                .build();

        request = new CustomerRequest(
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );

        response = new CustomerResponse(
                1L,
                "Juan",
                "Pérez",
                "juan@example.com",
                "+51987654321",
                true
        );
    }

    @Test
    void shouldCreateCustomerSuccessfully() {

        when(customerRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(false);

        when(customerMapper.toEntity(request))
                .thenReturn(customer);

        when(customerRepository.save(customer))
                .thenReturn(customer);

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        CustomerResponse result = customerService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("juan@example.com", result.email());
        assertTrue(result.active());

        verify(customerRepository).existsByEmailIgnoreCase(
                request.email()
        );

        verify(customerRepository).save(customer);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(customerRepository.existsByEmailIgnoreCase(request.email()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.create(request)
        );

        verify(customerMapper, never()).toEntity(any(CustomerRequest.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldFindCustomerByIdSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        CustomerResponse result = customerService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Juan", result.firstName());

        verify(customerRepository).findById(1L);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        when(customerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.findById(99L)
        );

        verify(customerMapper, never()).toResponse(any(Customer.class));
    }

    @Test
    void shouldFindActiveCustomersWithoutSearch() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Customer> customerPage = new PageImpl<>(
                List.of(customer),
                pageable,
                1
        );

        when(customerRepository.findByActiveTrue(pageable))
                .thenReturn(customerPage);

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        Page<CustomerResponse> result = customerService.findAll(
                null,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().id());

        verify(customerRepository).findByActiveTrue(pageable);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldFindCustomersBySearch() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Customer> customerPage = new PageImpl<>(
                List.of(customer),
                pageable,
                1
        );

        when(
                customerRepository
                        .findByActiveTrueAndFirstNameContainingIgnoreCaseOrActiveTrueAndLastNameContainingIgnoreCase(
                                "Juan",
                                "Juan",
                                pageable
                        )
        ).thenReturn(customerPage);

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        Page<CustomerResponse> result = customerService.findAll(
                "Juan",
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Juan", result.getContent().getFirst().firstName());

        verify(customerRepository)
                .findByActiveTrueAndFirstNameContainingIgnoreCaseOrActiveTrueAndLastNameContainingIgnoreCase(
                        "Juan",
                        "Juan",
                        pageable
                );
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(customer))
                .thenReturn(customer);

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        CustomerResponse result = customerService.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("juan@example.com", result.email());

        verify(customerMapper).updateEntity(customer, request);
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentCustomer() {

        when(customerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.update(99L, request)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldDeactivateActiveCustomer() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        customerService.delete(1L);

        assertFalse(customer.getActive());

        verify(customerRepository).save(customer);
    }

    @Test
    void shouldNotDeactivateAlreadyInactiveCustomer() {

        customer.setActive(false);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThrows(
                IllegalStateException.class,
                () -> customerService.delete(1L)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }
}