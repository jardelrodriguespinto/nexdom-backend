package br.com.product.nextdomtest.service;

import br.com.product.nextdomtest.dto.CreatedMovimentoEstoqueDto;
import br.com.product.nextdomtest.dto.MovimentoEstoqueDto;
import br.com.product.nextdomtest.enums.TipoMovimentacao;
import br.com.product.nextdomtest.exception.MovimentacaoNaoEncontradaException;
import br.com.product.nextdomtest.exception.ProdutoNaoEncontradoException;
import br.com.product.nextdomtest.exception.TipoMovimentacaoInvalidoException;
import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;
import br.com.product.nextdomtest.repository.MovimentoEstoqueRepository;
import br.com.product.nextdomtest.repository.ProdutoRepository;
import br.com.product.nextdomtest.strategy.MovimentacaoStrategy;
import br.com.product.nextdomtest.strategy.MovimentacaoStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovimentoEstoqueService
{
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoStrategyFactory movimentacaoStrategyFactory;

    @Autowired
    public MovimentoEstoqueService(
            MovimentoEstoqueRepository movimentoEstoqueRepository,
            ProdutoRepository produtoRepository,
            MovimentacaoStrategyFactory movimentacaoStrategyFactory
    )
    {
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
        this.produtoRepository = produtoRepository;
        this.movimentacaoStrategyFactory = movimentacaoStrategyFactory;
    }

    @Transactional
    public MovimentoEstoqueDto registrarMovimento(MovimentoEstoqueDto dto)
    {
        MovimentoEstoque movimento = converterDtoParaEntidade(dto);

        Produto produto = movimento.getProduto();
        MovimentacaoStrategy estrategiaMovimentacao = obterEstrategiaMovimentacao(movimento.getTipo());

        executarMovimentacao(estrategiaMovimentacao, produto, movimento);
        salvarProduto(produto);
        definirDataMovimentacao(movimento);

        MovimentoEstoque movimentoSalvo = salvarMovimento(movimento);
        return converterEntidadeParaDto(movimentoSalvo);
    }
    public MovimentoEstoqueDto buscarPorId(Long id)
    {
        MovimentoEstoque movimento = movimentoEstoqueRepository.findById(id)
                .orElseThrow(() -> new MovimentacaoNaoEncontradaException("Movimentação não encontrada com ID: " + id));

        return converterEntidadeParaDto(movimento);
    }

    public List<CreatedMovimentoEstoqueDto> listarPorProduto(Long produtoId)
    {
        Produto produto = buscarProdutoPorId(produtoId);

        return movimentoEstoqueRepository.findByProduto(produto)
                .stream()
                .map(this::converterEntidadeParaDetalhesDto)
                .collect(Collectors.toList());
    }

    public List<CreatedMovimentoEstoqueDto> listarTodos()
    {
        return movimentoEstoqueRepository.findAll()
                .stream()
                .map(this::converterEntidadeParaDetalhesDto)
                .collect(Collectors.toList());
    }

    private MovimentoEstoqueDto converterEntidadeParaDto(MovimentoEstoque movimento)
    {
        return new MovimentoEstoqueDto(
                movimento.getId(),
                movimento.getProduto().getId(),
                movimento.getTipo(),
                movimento.getValorVenda(),
                movimento.getQuantidade(),
                movimento.getDataMovimentacao()
        );
    }

    private CreatedMovimentoEstoqueDto converterEntidadeParaDetalhesDto(MovimentoEstoque movimento)
    {
        return new CreatedMovimentoEstoqueDto(
                movimento.getId(),
                movimento.getProduto(),
                movimento.getTipo(),
                movimento.getValorVenda(),
                movimento.getQuantidade(),
                movimento.getDataMovimentacao()
        );
    }

    private MovimentoEstoque converterDtoParaEntidade(MovimentoEstoqueDto dto)
    {
        Produto produto = buscarProdutoPorId(dto.produtoId());

        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.setId(dto.id());
        movimento.setProduto(produto);
        movimento.setTipo(dto.tipo());
        movimento.setValorVenda(dto.valorVenda());
        movimento.setQuantidade(dto.quantidade());
        movimento.setDataMovimentacao(dto.dataMovimentacao());
        return movimento;
    }

    private Produto buscarProdutoPorId(Long produtoId)
    {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado com ID: " + produtoId));
    }

    private MovimentacaoStrategy obterEstrategiaMovimentacao(TipoMovimentacao tipoMovimentacao)
    {
        try
        {
            return movimentacaoStrategyFactory.getStrategy(tipoMovimentacao);
        }
        catch (Exception e)
        {
            throw new TipoMovimentacaoInvalidoException("Tipo de movimentação inválido: " + tipoMovimentacao);
        }
    }

    private void executarMovimentacao(MovimentacaoStrategy estrategia, Produto produto, MovimentoEstoque movimento)
    {
        estrategia.movimentar(produto, movimento);
    }

    private void salvarProduto(Produto produto)
    {
        produtoRepository.save(produto);
    }

    private void definirDataMovimentacao(MovimentoEstoque movimento)
    {
        movimento.setDataMovimentacao(LocalDateTime.now());
    }

    private MovimentoEstoque salvarMovimento(MovimentoEstoque movimento)
    {
        return movimentoEstoqueRepository.save(movimento);
    }
}