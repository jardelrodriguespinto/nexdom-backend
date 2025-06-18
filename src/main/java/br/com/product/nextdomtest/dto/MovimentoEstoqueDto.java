package br.com.product.nextdomtest.dto;

import br.com.product.nextdomtest.enums.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentoEstoqueDto
    (
            Long id,

            @NotNull(message = "Produto é obrigatório")
            Long produtoId,

            @NotNull(message = "Tipo de movimentação é obrigatório")
            TipoMovimentacao tipo,

            @DecimalMin(value = "0.0", inclusive = true, message = "Valor de venda não pode ser negativo")
            BigDecimal valorVenda,

            @NotNull(message = "Quantidade movimentada é obrigatória")
            @Min(value = 1, message = "Quantidade movimentada deve ser no mínimo 1")
            Integer quantidade,

            LocalDateTime dataMovimentacao
    ) {
}
