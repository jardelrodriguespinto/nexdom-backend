package br.com.product.nextdomtest.strategy;

import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;

public interface MovimentacaoStrategy
{
     void movimentar(Produto produto, MovimentoEstoque movimento);
}
