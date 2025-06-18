package br.com.product.nextdomtest.strategy;

import br.com.product.nextdomtest.enums.TipoMovimentacao;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class MovimentacaoStrategyFactory {

    private final Map<TipoMovimentacao, MovimentacaoStrategy> strategies;

    public MovimentacaoStrategyFactory(
            EntradaStrategy entradaStrategy,
            SaidaStrategy saidaStrategy
    ) {
        strategies = new EnumMap<>(TipoMovimentacao.class);
        strategies.put(TipoMovimentacao.ENTRADA, entradaStrategy);
        strategies.put(TipoMovimentacao.SAIDA, saidaStrategy);
    }

    public MovimentacaoStrategy getStrategy(TipoMovimentacao tipo) {
        MovimentacaoStrategy strategy = strategies.get(tipo);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipo de movimentação não suportado: " + tipo);
        }
        return strategy;
    }
}
