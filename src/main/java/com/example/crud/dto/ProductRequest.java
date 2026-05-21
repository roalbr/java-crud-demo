package com.example.crud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para criação/atualização de Produto.
 * Padrão DTO — separa a camada de transporte da entidade JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private String description;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private BigDecimal price;

    @NotBlank(message = "Categoria é obrigatória")
    private String category;

    @NotBlank(message = "SKU é obrigatório")
    private String sku;

    private Integer stockQuantity;
}
