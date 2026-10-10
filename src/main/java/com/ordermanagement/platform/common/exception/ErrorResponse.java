package com.ordermanagement.platform.common.exception;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "ErrorResponse",
        description = "Estructura estándar utilizada para representar errores de la API")
public record ErrorResponse(

        @Schema(description = "Código HTTP del error",
                example = "404")
        int status,

        @Schema(description = "Mensaje descriptivo del error",
                example = "Producto no encontrado con id: 10")
        String message,

        @Schema(description = "Fecha y hora en que ocurrió el error",
                example = "2026-09-13T12:30:00")
        LocalDateTime timestamp
) {
}