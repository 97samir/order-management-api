package com.ordermanagement.platform.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// record en ves de getters and setter - nos permite simplemente representar
@Schema(name = "CategoryRequest",
        description = "Datos necesarios para crear o actualizar una categoría")
public record CategoryRequest(

        @Schema(description = "Nombre de la categoría",
                example = "Electrónica",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Schema(description = "Descripción de la categoría",
                example = "Productos electrónicos y tecnológicos",
                maxLength = 255)
        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description,

        @Schema(description = "Indica si la categoría está activa",
                example = "true",
                defaultValue = "true")
        Boolean active
) {
}