package com.pedro.marketplace.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProdutoRequestDTO(

        @NotBlank(message = "O nome do produto não pode ficar em branco")
        String nome,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.0", message = "O preço não pode ser negativo")
        BigDecimal preco,

        @Size(min = 10, message = "A descrição precisa ter pelo menos 10 caracteres")
        String descricao,

        String imagemUrl,

        List<String> imagemUrls

) {}
