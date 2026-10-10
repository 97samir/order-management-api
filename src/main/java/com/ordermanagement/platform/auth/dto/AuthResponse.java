package com.ordermanagement.platform.auth.dto;

import com.ordermanagement.platform.auth.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse",
        description = "Respuesta generada después de una autenticación exitosa")
public record AuthResponse(

        @Schema(description = "Token de acceso JWT",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Identificador del usuario",
                example = "1")
        Long userId,

        @Schema(description = "Nombre del usuario",
                example = "Juan Pérez")
        String name,

        @Schema(description = "Correo electrónico del usuario",
                example = "juan.perez@example.com")
        String email,

        @Schema(description = "Rol asignado al usuario",
                example = "CUSTOMER")
        Role role
) {
}