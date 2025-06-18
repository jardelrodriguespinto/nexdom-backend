package br.com.product.nextdomtest.exception;

public class TipoMovimentacaoInvalidoException extends RuntimeException
{
    public TipoMovimentacaoInvalidoException(String tipo)
    {
        super("Tipo de movimentação inválido: " + tipo);
    }
}
