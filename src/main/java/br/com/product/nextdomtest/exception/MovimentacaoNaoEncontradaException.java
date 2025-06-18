package br.com.product.nextdomtest.exception;

public class MovimentacaoNaoEncontradaException extends RuntimeException
{
    public MovimentacaoNaoEncontradaException(String mensagem)
    {
        super(mensagem);
    }
}
