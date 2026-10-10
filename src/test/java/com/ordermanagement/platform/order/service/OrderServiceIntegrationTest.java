package com.ordermanagement.platform.order.service;

import com.ordermanagement.platform.common.exception.InvalidOrderStateException;
import com.ordermanagement.platform.customer.entity.Customer;
import com.ordermanagement.platform.customer.repository.CustomerRepository;
import com.ordermanagement.platform.order.dto.OrderItemRequest;
import com.ordermanagement.platform.order.dto.OrderRequest;
import com.ordermanagement.platform.order.dto.OrderResponse;
import com.ordermanagement.platform.order.entity.Order;
import com.ordermanagement.platform.order.enums.OrderStatus;
import com.ordermanagement.platform.order.repository.OrderRepository;
import com.ordermanagement.platform.orderItem.entity.OrderItem;
import com.ordermanagement.platform.product.entity.Product;
import com.ordermanagement.platform.product.repository.ProductRepository;
import com.ordermanagement.platform.category.entity.Category;
import com.ordermanagement.platform.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("order_management_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);

        registry.add("spring.datasource.username",
                postgres::getUsername);

        registry.add("spring.datasource.password",
                postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "create-drop");
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;


    @BeforeEach
    void setUp() {

        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @Transactional
    void shouldCreateOrderAndUpdateStockSuccessfully() {

        // 1. Crear categoría
        Category category = Category.builder()
                .name("Electrónica")
                .description("Productos electrónicos")
                .active(true)
                .build();

        category = categoryRepository.save(category);

        // 2. Crear producto
        Product product = Product.builder()
                .name("Laptop")
                .description("Laptop para trabajo")
                .price(new BigDecimal("1500.00"))
                .stock(10)
                .active(true)
                .category(category)
                .build();

        product = productRepository.save(product);

        // 3. Crear cliente
        Customer customer = Customer.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@test.com")
                .phone("+51999999999")
                .active(true)
                .build();

        customer = customerRepository.save(customer);

        // 4. Crear pedido
        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                2
                        )
                )
        );

        // 5. Ejecutar servicio real
        OrderResponse response = orderService.create(request);

        // 6. Verificar respuesta
        assertThat(response.id()).isNotNull();
        assertThat(response.customerId())
                .isEqualTo(customer.getId());

        assertThat(response.status())
                .isEqualTo(OrderStatus.PENDING);

        assertThat(response.total())
                .isEqualByComparingTo("3000.00");

        assertThat(response.items())
                .hasSize(1);

        // 7. Verificar stock
        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(updatedProduct.getStock())
                .isEqualTo(8);

        // 8. Verificar Order en PostgreSQL
        Order savedOrder = orderRepository
                .findById(response.id())
                .orElseThrow();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.PENDING);

        assertThat(savedOrder.getTotal())
                .isEqualByComparingTo("3000.00");

        // 9. Verificar OrderItem
        assertThat(savedOrder.getItems())
                .hasSize(1);

        OrderItem savedItem = savedOrder.getItems()
                .get(0);

        assertThat(savedItem.getProduct().getId())
                .isEqualTo(product.getId());

        assertThat(savedItem.getQuantity())
                .isEqualTo(2);

        assertThat(savedItem.getUnitPrice())
                .isEqualByComparingTo("1500.00");

        assertThat(savedItem.getSubtotal())
                .isEqualByComparingTo("3000.00");
    }

    // Crear pedido - descontar stock - cancelar pedido - restaurar stock
    @Test
    @Transactional
    void shouldCancelOrderAndRestoreStockSuccessfully() {

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Electrónica")
                        .description("Productos electrónicos")
                        .active(true)
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Mouse inalámbrico")
                        .description("Mouse inalámbrico")
                        .price(new BigDecimal("100.00"))
                        .stock(10)
                        .active(true)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .firstName("Juan")
                        .lastName("Pérez")
                        .email("juan.perez@test.com")
                        .phone("999999999")
                        .active(true)
                        .build()
        );

        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                2
                        )
                )
        );

        // Crear pedido
        OrderResponse createdOrder = orderService.create(request);

        // El stock debe haber disminuido de 10 a 8
        Product productAfterCreate = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(productAfterCreate.getStock())
                .isEqualTo(8);

        // Cancelar pedido
        OrderResponse cancelledOrder =
                orderService.cancel(createdOrder.id());

        assertThat(cancelledOrder.status())
                .isEqualTo(OrderStatus.CANCELLED);

        // El stock debe regresar de 8 a 10
        Product productAfterCancel = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(productAfterCancel.getStock())
                .isEqualTo(10);

        // Verificar que el pedido quedó CANCELLED
        Order savedOrder = orderRepository
                .findById(createdOrder.id())
                .orElseThrow();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    // reglas de transición de estados
    // (PENDING - CONFIRMED - COMPLETED - estados inválidos)
    @Test
    @Transactional
    void shouldConfirmAndCompleteOrderSuccessfully() {

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Accesorios")
                        .description("Accesorios tecnológicos")
                        .active(true)
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Teclado mecánico")
                        .description("Teclado mecánico RGB")
                        .price(new BigDecimal("250.00"))
                        .stock(10)
                        .active(true)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .firstName("Carlos")
                        .lastName("Gómez")
                        .email("carlos.gomez@test.com")
                        .phone("988888888")
                        .active(true)
                        .build()
        );

        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                2
                        )
                )
        );

        // Crear pedido
        OrderResponse createdOrder = orderService.create(request);

        assertThat(createdOrder.status())
                .isEqualTo(OrderStatus.PENDING);

        // Confirmar pedido
        OrderResponse confirmedOrder =
                orderService.confirm(createdOrder.id());

        assertThat(confirmedOrder.status())
                .isEqualTo(OrderStatus.CONFIRMED);

        // Completar pedido
        OrderResponse completedOrder =
                orderService.complete(createdOrder.id());

        assertThat(completedOrder.status())
                .isEqualTo(OrderStatus.COMPLETED);

        // Verificar estado persistido en PostgreSQL
        Order savedOrder = orderRepository
                .findById(createdOrder.id())
                .orElseThrow();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.COMPLETED);
    }

    // transición inválida
    @Test
    @Transactional
    void shouldNotCompletePendingOrder() {

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Monitores")
                        .description("Monitores")
                        .active(true)
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Monitor 24 pulgadas")
                        .description("Monitor Full HD")
                        .price(new BigDecimal("700.00"))
                        .stock(10)
                        .active(true)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .firstName("Pedro")
                        .lastName("Ramírez")
                        .email("pedro.ramirez@test.com")
                        .phone("977777777")
                        .active(true)
                        .build()
        );

        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                1
                        )
                )
        );

        OrderResponse createdOrder =
                orderService.create(request);

        assertThat(createdOrder.status())
                .isEqualTo(OrderStatus.PENDING);

        assertThatThrownBy(() ->
                orderService.complete(createdOrder.id())
        )
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage(
                        "Solo se puede completar un pedido en estado CONFIRMED"
                );

        // Verificar que el pedido sigue PENDING
        Order savedOrder = orderRepository
                .findById(createdOrder.id())
                .orElseThrow();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.PENDING);
    }

    // casos inválidos cancel
    @Test
    @Transactional
    void shouldNotCancelCompletedOrder() {

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Laptops")
                        .description("Computadoras portátiles")
                        .active(true)
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Laptop profesional")
                        .description("Laptop para trabajo")
                        .price(new BigDecimal("3500.00"))
                        .stock(10)
                        .active(true)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .firstName("Ana")
                        .lastName("Torres")
                        .email("ana.torres@test.com")
                        .phone("966666666")
                        .active(true)
                        .build()
        );

        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                1
                        )
                )
        );

        // Crear pedido
        OrderResponse createdOrder =
                orderService.create(request);

        // Confirmar
        orderService.confirm(createdOrder.id());

        // Completar
        OrderResponse completedOrder =
                orderService.complete(createdOrder.id());

        assertThat(completedOrder.status())
                .isEqualTo(OrderStatus.COMPLETED);

        // Intentar cancelar un pedido COMPLETED
        assertThatThrownBy(() ->
                orderService.cancel(createdOrder.id())
        )
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage(
                        "No se puede cancelar un pedido con estado COMPLETED"
                );

        // Verificar que permanece COMPLETED
        Order savedOrder = orderRepository
                .findById(createdOrder.id())
                .orElseThrow();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.COMPLETED);
    }

    // casoCANCELLED - CONFIRMED
    @Test
    @Transactional
    void shouldNotConfirmCancelledOrder() {

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Tablets")
                        .description("Tablets")
                        .active(true)
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Tablet 10 pulgadas")
                        .description("Tablet para uso general")
                        .price(new BigDecimal("900.00"))
                        .stock(10)
                        .active(true)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .firstName("Luis")
                        .lastName("Mendoza")
                        .email("luis.mendoza@test.com")
                        .phone("955555555")
                        .active(true)
                        .build()
        );

        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                1
                        )
                )
        );

        // Crear pedido
        OrderResponse createdOrder =
                orderService.create(request);

        // Cancelar
        OrderResponse cancelledOrder =
                orderService.cancel(createdOrder.id());

        assertThat(cancelledOrder.status())
                .isEqualTo(OrderStatus.CANCELLED);

        // Intentar confirmar un pedido cancelado
        assertThatThrownBy(() ->
                orderService.confirm(createdOrder.id())
        )
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage(
                        "Solo se puede confirmar un pedido en estado PENDING"
                );

        // Verificar que permanece CANCELLED
        Order savedOrder = orderRepository
                .findById(createdOrder.id())
                .orElseThrow();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @Transactional
    void shouldNotCreateOrderWithInactiveProduct() {

        Category category = categoryRepository.save(
                Category.builder()
                        .name("Accesorios")
                        .description("Accesorios tecnológicos")
                        .active(true)
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("Mouse desactivado")
                        .description("Mouse no disponible")
                        .price(new BigDecimal("100.00"))
                        .stock(10)
                        .active(false)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .firstName("Juan")
                        .lastName("Pérez")
                        .email("juan.inactive@test.com")
                        .phone("999999999")
                        .active(true)
                        .build()
        );

        OrderRequest request = new OrderRequest(
                customer.getId(),
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                1
                        )
                )
        );

        assertThatThrownBy(() ->
                orderService.create(request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "El producto no está disponible: Mouse desactivado"
                );

        Product savedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(savedProduct.getStock())
                .isEqualTo(10);

        assertThat(orderRepository.count())
                .isZero();
    }
}