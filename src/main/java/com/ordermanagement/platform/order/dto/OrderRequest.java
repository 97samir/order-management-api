package com.ordermanagement.platform.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(name = "OrderRequest",
        description = "Datos necesarios para crear un pedido")
public record OrderRequest(

        @Schema(description = "Identificador del cliente que realiza el pedido",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El cliente es obligatorio")
        Long customerId,

        @Schema(description = "Lista de productos incluidos en el pedido",
                example = """
                        [
                                {
                                "productId": 1,
                                "quantity": 2
                                },
                                {
                                "productId": 2,
                                "quantity": 1
                                }
                        ]
                """,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "El pedido debe contener al menos un producto")
        List<@Valid OrderItemRequest> items
) {
}