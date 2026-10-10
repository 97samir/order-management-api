package com.ordermanagement.platform.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "OrderItemResponse",
        description = "Detalle de un producto incluido en un pedido")
public record OrderItemResponse(

        @Schema(description = "Identificador del detalle del pedido",
                example = "1")
        Long id,

        @Schema(description = "Identificador del producto",
                example = "1")
        Long productId,

        @Schema(description = "Nombre del producto",
                example = "Laptop Lenovo ThinkPad")
        String productName,

        @Schema(description = "Cantidad solicitada",
                example = "2")
        Integer quantity,

        @Schema(description = "Precio unitario utilizado en el pedido",
                example = "2499.90")
        BigDecimal unitPrice,

        @Schema(description = "Subtotal del detalle",
                example = "4999.80")
        BigDecimal subtotal
) {
}