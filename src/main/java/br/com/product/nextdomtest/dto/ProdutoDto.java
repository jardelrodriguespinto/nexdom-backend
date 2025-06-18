package br.com.product.nextdomtest.dto;

import br.com.product.nextdomtest.enums.TipoProduto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoDto
(
    Long id,

    @NotBlank(message = "Código é obrigatório")
    String codigo,

    @NotBlank(message = "Descrição é obrigatória")
    String descricao,

    @NotNull(message = "Tipo do produto é obrigatório")
    TipoProduto tipo,

    @NotNull(message = "Valor do fornecedor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    BigDecimal valorFornecedor,

    @NotNull(message = "Quantidade em estoque é obrigatória")
    @Min(value = 0, message = "Quantidade em estoque não pode ser negativa")
    Integer quantidadeEstoque

) {}
