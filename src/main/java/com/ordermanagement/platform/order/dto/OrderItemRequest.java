package com.ordermanagement.platform.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "OrderItemRequest",
        description = "Producto y cantidad incluidos en un pedido")
public record OrderItemRequest(

        @Schema(description = "Identificador del producto",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El producto es obligatorio")
        Long productId,

        @Schema(description = "Cantidad de unidades del producto",
                example = "2",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1,
                message = "La cantidad debe ser como mínimo 1")
        Integer quantity
) {
}