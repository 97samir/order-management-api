package com.ordermanagement.platform.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleResourceNotFoundException() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Producto no encontrado con id: 1"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleResourceNotFound(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(404);

        assertThat(response.getBody().message())
                .isEqualTo("Producto no encontrado con id: 1");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleDuplicateResourceException() {

        DuplicateResourceException exception =
                new DuplicateResourceException(
                        "Ya existe un producto con ese nombre"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleDuplicateResource(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(409);

        assertThat(response.getBody().message())
                .isEqualTo("Ya existe un producto con ese nombre");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleIllegalArgumentException() {

        IllegalArgumentException exception =
                new IllegalArgumentException(
                        "El argumento no es válido"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleIllegalArgument(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().message())
                .isEqualTo("El argumento no es válido");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleIllegalStateException() {

        IllegalStateException exception =
                new IllegalStateException(
                        "El cliente ya está inactivo"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleIllegalState(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(409);

        assertThat(response.getBody().message())
                .isEqualTo("El cliente ya está inactivo");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleInsufficientStockException() {

        InsufficientStockException exception =
                new InsufficientStockException(
                        "Stock insuficiente para el producto: Laptop"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInsufficientStock(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(409);

        assertThat(response.getBody().message())
                .isEqualTo("Stock insuficiente para el producto: Laptop");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleInvalidOrderStateException() {

        InvalidOrderStateException exception =
                new InvalidOrderStateException(
                        "Solo se puede completar un pedido en estado CONFIRMED"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInvalidOrderState(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(409);

        assertThat(response.getBody().message())
                .isEqualTo(
                        "Solo se puede completar un pedido en estado CONFIRMED"
                );

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleInvalidOrderRequestException() {

        InvalidOrderRequestException exception =
                new InvalidOrderRequestException(
                        "El producto no puede aparecer más de una vez"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInvalidOrderRequest(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().message())
                .isEqualTo(
                        "El producto no puede aparecer más de una vez"
                );

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleInvalidJsonBody() {

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleMessageNotReadable(null);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().message())
                .isEqualTo(
                        "El cuerpo de la solicitud no tiene un formato válido"
                );

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleUnexpectedException() {

        RuntimeException exception =
                new RuntimeException("Detalle interno que no debe exponerse");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(500);

        assertThat(response.getBody().message())
                .isEqualTo(
                        "Ocurrió un error interno en el servidor"
                );

        assertThat(response.getBody().message())
                .doesNotContain("Detalle interno");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldHandleValidationException() throws NoSuchMethodException {

        Object target = new Object();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "categoryRequest");

        bindingResult.addError(
                new FieldError(
                        "categoryRequest",
                        "name",
                        "El nombre es obligatorio"
                )
        );

        Method method = ValidationTestController.class
                .getDeclaredMethod("testMethod", String.class);

        MethodParameter methodParameter =
                new MethodParameter(method, 0);

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        methodParameter,
                        bindingResult
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleValidation(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().message())
                .isEqualTo("name: El nombre es obligatorio");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    @Test
    void shouldReturnFirstValidationError() throws NoSuchMethodException {

        Object target = new Object();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "categoryRequest");

        bindingResult.addError(
                new FieldError(
                        "categoryRequest",
                        "name",
                        "El nombre es obligatorio"
                )
        );

        bindingResult.addError(
                new FieldError(
                        "categoryRequest",
                        "description",
                        "La descripción no puede superar los 255 caracteres"
                )
        );

        Method method = ValidationTestController.class
                .getDeclaredMethod("testMethod", String.class);

        MethodParameter methodParameter =
                new MethodParameter(method, 0);

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        methodParameter,
                        bindingResult
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleValidation(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().message())
                .isEqualTo("name: El nombre es obligatorio");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    private static class ValidationTestController {

        @SuppressWarnings("unused")
        public void testMethod(String value) {
        }
    }
}