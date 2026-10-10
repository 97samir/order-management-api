package com.ordermanagement.platform.order.dto;

import com.ordermanagement.platform.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "OrderResponse",
        description = "Información completa de un pedido")
public record OrderResponse(

        @Schema(description = "Identificador único del pedido",
                example = "1")
        Long id,

        @Schema(description = "Identificador del cliente",
                example = "1")
        Long customerId,

        @Schema(description = "Nombre completo del cliente",
                example = "Juan Pérez")
        String customerName,

        @Schema(description = "Total del pedido",
                example = "4999.80")
        BigDecimal total,

        @Schema(description = "Estado actual del pedido. Puede ser PENDING, CONFIRMED, CANCELLED o COMPLETED",
                example = "PENDING")
        OrderStatus status,

        @Schema(description = "Fecha y hora de creación del pedido",
                example = "2026-09-14T12:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Productos incluidos en el pedido")
        List<OrderItemResponse> items
) {
}