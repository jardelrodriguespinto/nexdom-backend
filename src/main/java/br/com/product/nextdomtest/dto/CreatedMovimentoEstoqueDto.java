package br.com.product.nextdomtest.dto;

import br.com.product.nextdomtest.enums.TipoMovimentacao;
import br.com.product.nextdomtest.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatedMovimentoEstoqueDto
        (
                Long id,

                Produto produto,

                TipoMovimentacao tipo,

                BigDecimal valorVenda,

                Integer quantidade,

                LocalDateTime dataMovimentacao
        ) {
}

