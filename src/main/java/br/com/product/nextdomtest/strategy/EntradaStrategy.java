package br.com.product.nextdomtest.strategy;

import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class EntradaStrategy implements MovimentacaoStrategy
{
    @Override
    public void movimentar(Produto produto, MovimentoEstoque movimento)
    {
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + movimento.getQuantidade());
    }
}

