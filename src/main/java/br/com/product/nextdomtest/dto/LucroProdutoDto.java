package br.com.product.nextdomtest.dto;

import br.com.product.nextdomtest.model.Produto;

import java.math.BigDecimal;

public record LucroProdutoDto (
        Produto produto,

        String descricao,

        Integer quantidadeSaida,

        BigDecimal valorCompraUnitario,

        BigDecimal valorVendaUnitario,

        BigDecimal lucroUnitario,

        BigDecimal lucroTotal
) {}