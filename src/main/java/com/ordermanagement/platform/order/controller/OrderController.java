package com.ordermanagement.platform.order.controller;

import com.ordermanagement.platform.common.exception.ErrorResponse;
import com.ordermanagement.platform.order.dto.OrderRequest;
import com.ordermanagement.platform.order.dto.OrderResponse;
import com.ordermanagement.platform.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders",
        description = "Operaciones para gestionar pedidos y sus estados")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Crear un pedido",
            description = """
                    Crea un pedido para un cliente.
                    El pedido inicia en estado PENDING,
                    calcula automáticamente los subtotales y el total,
                    y descuenta el stock de los productos.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados no son válidos o el pedido contiene productos duplicados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El cliente o alguno de los productos no existe",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El cliente o producto está inactivo, o no existe stock suficiente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Valid @RequestBody OrderRequest request
    ) {
        return orderService.create(request);
    }

    @Operation(
            summary = "Obtener un pedido",
            description = "Obtiene un pedido mediante su identificador"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permiso para consultar este pedido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public OrderResponse findById(
            @PathVariable Long id
    ) {
        return orderService.findById(id);
    }

    @Operation(
            summary = "Listar pedidos",
            description = "Obtiene pedidos con paginación y filtro opcional por cliente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos obtenidos correctamente"
            )
    })
    @GetMapping
    public Page<OrderResponse> findAll(
            @RequestParam(required = false) Long customerId,
            @ParameterObject Pageable pageable
    ) {
        return orderService.findAll(customerId, pageable);
    }

    @Operation(
            summary = "Confirmar un pedido",
            description = "Cambia el estado de un pedido de PENDING a CONFIRMED"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido confirmado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El usuario no está autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para crear productos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El pedido no se encuentra en estado PENDING",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{id}/confirm")
    public OrderResponse confirm(
            @PathVariable Long id
    ) {
        return orderService.confirm(id);
    }

    @Operation(
            summary = "Cancelar un pedido",
            description = """
                    Cancela un pedido en estado PENDING o CONFIRMED.
                    El stock de los productos del pedido es restaurado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido cancelado correctamente y stock restaurado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El pedido no puede cancelarse debido a su estado actual",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(
            @PathVariable Long id
    ) {
        return orderService.cancel(id);
    }

    @Operation(
            summary = "Completar un pedido",
            description = "Cambia el estado de un pedido de CONFIRMED a COMPLETED"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido completado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El usuario no está autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para crear productos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El pedido no se encuentra en estado CONFIRMED",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{id}/complete")
    public OrderResponse complete(
            @PathVariable Long id
    ) {
        return orderService.complete(id);
    }
}