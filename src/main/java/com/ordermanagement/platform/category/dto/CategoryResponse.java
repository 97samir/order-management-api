package com.ordermanagement.platform.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CategoryResponse",
        description = "Información de una categoría")
public record CategoryResponse(

        @Schema(description = "Identificador único de la categoría",
                example = "1")
        Long id,

        @Schema(description = "Nombre de la categoría",
                example = "Electrónica")
        String name,

        @Schema(description = "Descripción de la categoría",
                example = "Productos electrónicos y tecnológicos")
        String description,

        @Schema(description = "Indica si la categoría está activa",
                example = "true")
        Boolean active
) {
}