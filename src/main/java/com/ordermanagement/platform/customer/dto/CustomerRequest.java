package com.ordermanagement.platform.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "CustomerRequest",
        description = "Datos necesarios para crear o actualizar un cliente")
public record CustomerRequest(

        @Schema(description = "Nombre del cliente",
                example = "Juan",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String firstName,

        @Schema(description = "Apellido del cliente",
                example = "Pérez",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String lastName,

        @Schema(description = "Correo electrónico del cliente",
                example = "juan.perez@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email,

        @Schema(description = "Número telefónico del cliente",
                example = "+51987654321")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$",
                message = "El teléfono no tiene un formato válido")
        String phone,

        @Schema(description = "Indica si el cliente está activo",
                example = "true",
                defaultValue = "true")
        Boolean active

) {
}