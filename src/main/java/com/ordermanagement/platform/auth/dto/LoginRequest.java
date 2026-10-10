package com.ordermanagement.platform.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest",
        description = "Credenciales necesarias para iniciar sesión")
public record LoginRequest(

        @Schema(description = "Correo electrónico del usuario",
                example = "juan.perez@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @Schema(description = "Contraseña del usuario",
                example = "Password123!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}