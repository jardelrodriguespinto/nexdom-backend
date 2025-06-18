package br.com.product.nextdomtest.service;

import br.com.product.nextdomtest.dto.ProdutoDto;
import br.com.product.nextdomtest.dto.LucroProdutoDto;
import br.com.product.nextdomtest.enums.TipoMovimentacao;
import br.com.product.nextdomtest.enums.TipoProduto;
import br.com.product.nextdomtest.exception.ProdutoNaoEncontradoException;
import br.com.product.nextdomtest.exception.OperacaoNaoPermitidaException;
import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;
import br.com.product.nextdomtest.repository.MovimentoEstoqueRepository;
import br.com.product.nextdomtest.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProdutoService
{
    private final ProdutoRepository produtoRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository,
                          MovimentoEstoqueRepository movimentoEstoqueRepository)
    {
        this.produtoRepository = produtoRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
    }

    @Transactional
    public ProdutoDto salvar(ProdutoDto dto)
    {
        Produto produto = converterDtoParaEntidade(dto);
        Produto produtoSalvo = produtoRepository.save(produto);
        return converterEntidadeParaDto(produtoSalvo);
    }

    public ProdutoDto buscarPorId(Long id)
    {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(id));
        return converterEntidadeParaDto(produto);
    }

    public List<ProdutoDto> listarTodos()
    {
        return produtoRepository.findAll()
                .stream()
                .map(this::converterEntidadeParaDto)
                .collect(Collectors.toList());
    }

    public List<ProdutoDto> listarPorTipo(TipoProduto tipo)
    {
        return produtoRepository.findByTipo(tipo)
                .stream()
                .map(this::converterEntidadeParaDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoDto atualizar(Long id, ProdutoDto dto)
    {
        validarExistenciaProduto(id);
        Produto produto = extrairAlteracoesDoDto(dto);
        produto.setId(id);
        Produto produtoAtualizado = produtoRepository.save(produto);
        return converterEntidadeParaDto(produtoAtualizado);
    }

    @Transactional
    public void deletar(Long id)
    {
        validarExistenciaProduto(id);

        if (temMovimentacoes(id))
        {
            throw new OperacaoNaoPermitidaException(
                    "Não é possível excluir o produto. Pois há movimentação deste produto."
            );
        }

        produtoRepository.deleteById(id);
    }

    public LucroProdutoDto consultarLucro(Long produtoId)
    {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + produtoId));

        Integer quantidadeSaida = calcularQuantidadeSaida(produto);
        BigDecimal valorCompraUnitario = calcularValorCompraUnitario(produto);
        BigDecimal valorVendaUnitario = calcularValorVendaUnitario(produto);
        BigDecimal resultado = valorVendaUnitario.subtract(valorCompraUnitario);
        BigDecimal lucroUnitario = resultado.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : resultado;        BigDecimal lucroTotal = calcularLucroProduto(produto);

        return new LucroProdutoDto(
                produto,
                produto.getDescricao(),
                quantidadeSaida,
                valorCompraUnitario,
                valorVendaUnitario,
                lucroUnitario,
                lucroTotal
        );
    }

    private boolean temMovimentacoes(Long produtoId)
    {
        return movimentoEstoqueRepository.existsByProdutoId(produtoId);
    }

    /**
     * Calcula o valor unitário de compra do produto baseado no valor total e quantidade inicial
     */
    private BigDecimal calcularValorCompraUnitario(Produto produto)
    {
        // Busca a primeira movimentação de ENTRADA para obter a quantidade inicial comprada
        Optional<MovimentoEstoque> primeiraEntrada = movimentoEstoqueRepository.findByProduto(produto)
                .stream()
                .filter(movimento -> movimento.getTipo() == TipoMovimentacao.ENTRADA)
                .findFirst();

        if (primeiraEntrada.isPresent())
        {
            int quantidadeComprada = primeiraEntrada.get().getQuantidade();
            return produto.getValorFornecedor().divide(BigDecimal.valueOf(quantidadeComprada), 2, RoundingMode.HALF_UP);
        }

        // Se não encontrar movimentação de entrada, assume que o valor já é unitário
        return produto.getValorFornecedor();
    }

    /**
     * Calcula o valor médio de venda unitário do produto
     */
    private BigDecimal calcularValorVendaUnitario(Produto produto)
    {
        List<MovimentoEstoque> saidas = movimentoEstoqueRepository.findByProduto(produto)
                .stream()
                .filter(movimento -> movimento.getTipo() == TipoMovimentacao.SAIDA)
                .toList();

        if (saidas.isEmpty())
        {
            return BigDecimal.ZERO;
        }

        BigDecimal valorTotalVendas = saidas.stream()
                .map(saida -> saida.getValorVenda().multiply(BigDecimal.valueOf(saida.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int quantidadeTotalVendida = saidas.stream()
                .mapToInt(MovimentoEstoque::getQuantidade)
                .sum();

        return valorTotalVendas.divide(BigDecimal.valueOf(quantidadeTotalVendida), 2, RoundingMode.HALF_UP);
    }

    /**
     * Retorna a quantidade total de saídas de um produto.
     */
    private Integer calcularQuantidadeSaida(Produto produto)
    {
        return movimentoEstoqueRepository.findByProduto(produto)
                .stream()
                .filter(movimento -> movimento.getTipo() == TipoMovimentacao.SAIDA)
                .mapToInt(MovimentoEstoque::getQuantidade)
                .sum();
    }

    /**
     * Retorna o lucro total de um produto usando o valor unitário correto de compra.
     */
    private BigDecimal calcularLucroProduto(Produto produto)
    {
        return movimentoEstoqueRepository.findByProduto(produto)
                .stream()
                .filter(movimento -> movimento.getTipo() == TipoMovimentacao.SAIDA)
                .map(movimento -> calcularLucroMovimentacao(movimento, produto))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula o lucro de uma movimentação específica usando o valor unitário correto de compra
     */
    private BigDecimal calcularLucroMovimentacao(MovimentoEstoque movimento, Produto produto)
    {
        BigDecimal valorCompraUnitario = calcularValorCompraUnitario(produto);
        BigDecimal lucroUnitario = movimento.getValorVenda().subtract(valorCompraUnitario);
        return lucroUnitario.multiply(BigDecimal.valueOf(movimento.getQuantidade()));
    }

    private ProdutoDto converterEntidadeParaDto(Produto produto)
    {
        return new ProdutoDto(
                produto.getId(),
                produto.getCodigo(),
                produto.getDescricao(),
                produto.getTipo(),
                produto.getValorFornecedor(),
                produto.getQuantidadeEstoque()
        );
    }

    private Produto converterDtoParaEntidade(ProdutoDto dto)
    {
        Produto produto = new Produto();
        produto.setId(dto.id());
        produto.setCodigo(dto.codigo());
        produto.setDescricao(dto.descricao());
        produto.setTipo(dto.tipo());
        produto.setValorFornecedor(dto.valorFornecedor());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
        return produto;
    }

    private Produto extrairAlteracoesDoDto(ProdutoDto dto)
    {
        Produto produto = new Produto();
        produto.setId(dto.id());
        produto.setCodigo(dto.codigo());
        produto.setDescricao(dto.descricao());
        produto.setTipo(dto.tipo());
        produto.setValorFornecedor(dto.valorFornecedor());
        return produto;
    }

    private void validarExistenciaProduto(Long id)
    {
        if (!produtoRepository.existsById(id))
        {
            throw new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + id);
        }
    }
}