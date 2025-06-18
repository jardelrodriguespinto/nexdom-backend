package br.com.product.nextdomtest.strategy;

import br.com.product.nextdomtest.exception.EstoqueInsuficienteException;
import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class SaidaStrategy implements MovimentacaoStrategy
{

    @Override
    public void movimentar(Produto produto, MovimentoEstoque movimento)
    {
        if (produto.getQuantidadeEstoque() < movimento.getQuantidade())
        {
            throw new EstoqueInsuficienteException("Estoque insuficiente para a saída do produto ID " + produto.getId());
        }

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - movimento.getQuantidade());
    }
}
