package com.ordermanagement.platform.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(name = "ProductRequest",
        description = "Datos necesarios para crear o actualizar un producto")
public record ProductRequest(

        @Schema(description = "Nombre del producto",
                example = "Laptop Lenovo ThinkPad",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @Schema(description = "Descripción del producto",
                example = "Laptop empresarial con procesador Intel Core i7",
                maxLength = 500)
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String description,

        @Schema(description = "Precio del producto",
                example = "2499.90",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal price,

        @Schema(description = "Cantidad disponible en inventario",
                example = "25",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Schema(description = "Indica si el producto está activo",
                example = "true",
                defaultValue = "true")
        Boolean active,

        @Schema(description = "Identificador de la categoría a la que pertenece el producto",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId

) {
}