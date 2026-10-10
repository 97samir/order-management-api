package com.ordermanagement.platform.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ProductResponse",
        description = "Información completa de un producto")
public record ProductResponse(

        @Schema(description = "Identificador único del producto",
                example = "1")
        Long id,

        @Schema(description = "Nombre del producto",
                example = "Laptop Lenovo ThinkPad")
        String name,

        @Schema(description = "Descripción del producto",
                example = "Laptop empresarial con procesador Intel Core i7")
        String description,

        @Schema(description = "Precio actual del producto",
                example = "2499.90")
        BigDecimal price,

        @Schema(description = "Cantidad disponible en inventario",
                example = "25")
        Integer stock,

        @Schema(description = "Indica si el producto está activo",
                example = "true")
        Boolean active,

        @Schema(description = "Identificador de la categoría",
                example = "1")
        Long categoryId,

        @Schema(description = "Nombre de la categoría",
                example = "Electrónica")
        String categoryName
) {
}