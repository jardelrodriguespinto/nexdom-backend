package br.com.product.nextdomtest.exception;

public class ProdutoNaoEncontradoException extends RuntimeException
{
    public ProdutoNaoEncontradoException(Long id)
    {
        super("Produto com ID " + id + " não encontrado.");
    }

    public ProdutoNaoEncontradoException(String mensagem)
    {
        super(mensagem);
    }
}
