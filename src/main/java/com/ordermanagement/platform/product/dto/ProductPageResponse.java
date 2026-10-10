package com.ordermanagement.platform.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ProductPageResponse",
        description = "Resultado paginado de productos")
public record ProductPageResponse(

        @Schema(description = "Lista de productos de la página actual")
        List<ProductResponse> content,

        @Schema(description = "Número de página actual",
                example = "0")
        int page,

        @Schema(description = "Cantidad de elementos por página",
                example = "10")
        int size,

        @Schema(description = "Cantidad total de productos",
                example = "35")
        long totalElements,

        @Schema(description = "Cantidad total de páginas",
                example = "4")
        int totalPages
) {
}