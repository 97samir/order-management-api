package com.ordermanagement.platform.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest",
        description = "Datos necesarios para registrar un usuario")
public record RegisterRequest(

        @Schema(description = "Nombre del usuario",
                example = "Juan Pérez",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Schema(description = "Correo electrónico del usuario",
                example = "juan.perez@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email,

        @Schema(description = "Contraseña del usuario",
                example = "Password123!",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 8)
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8,
                max = 100,
                message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password
) {
}