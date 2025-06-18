package br.com.product.nextdomtest.exception;

public class EstoqueInsuficienteException extends RuntimeException
{
    public EstoqueInsuficienteException(String mensagem)
    {
        super(mensagem);
    }
}
