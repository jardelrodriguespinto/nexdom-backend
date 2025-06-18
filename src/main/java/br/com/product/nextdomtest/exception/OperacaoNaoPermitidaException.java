package br.com.product.nextdomtest.exception;

public class OperacaoNaoPermitidaException extends RuntimeException
{

    public OperacaoNaoPermitidaException(String operacao)
    {
        super("Operação não permitida: " + operacao);
    }
}
