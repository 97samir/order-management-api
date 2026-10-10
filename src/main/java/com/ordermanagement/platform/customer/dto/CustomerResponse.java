package com.ordermanagement.platform.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CustomerResponse",
        description = "Información de un cliente")
public record CustomerResponse(

        @Schema(description = "Identificador único del cliente",
                example = "1")
        Long id,

        @Schema(description = "Nombre del cliente",
                example = "Juan")
        String firstName,

        @Schema(description = "Apellido del cliente",
                example = "Pérez")
        String lastName,

        @Schema(description = "Correo electrónico del cliente",
                example = "juan.perez@example.com")
        String email,

        @Schema(description = "Número telefónico del cliente",
                example = "+51987654321")
        String phone,

        @Schema(description = "Indica si el cliente está activo",
                example = "true")
        Boolean active
) {
}