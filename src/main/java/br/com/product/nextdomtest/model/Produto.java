package br.com.product.nextdomtest.model;

import br.com.product.nextdomtest.enums.TipoProduto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Produto
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoProduto tipo;

    private BigDecimal valorFornecedor;

    private Integer quantidadeEstoque;

}
