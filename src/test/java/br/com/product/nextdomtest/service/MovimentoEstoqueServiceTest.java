package br.com.product.nextdomtest.service;

import br.com.product.nextdomtest.dto.CreatedMovimentoEstoqueDto;
import br.com.product.nextdomtest.dto.MovimentoEstoqueDto;
import br.com.product.nextdomtest.enums.TipoMovimentacao;
import br.com.product.nextdomtest.enums.TipoProduto;
import br.com.product.nextdomtest.exception.MovimentacaoNaoEncontradaException;
import br.com.product.nextdomtest.exception.ProdutoNaoEncontradoException;
import br.com.product.nextdomtest.exception.TipoMovimentacaoInvalidoException;
import br.com.product.nextdomtest.model.MovimentoEstoque;
import br.com.product.nextdomtest.model.Produto;
import br.com.product.nextdomtest.repository.MovimentoEstoqueRepository;
import br.com.product.nextdomtest.repository.ProdutoRepository;
import br.com.product.nextdomtest.strategy.MovimentacaoStrategy;
import br.com.product.nextdomtest.strategy.MovimentacaoStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da classe MovimentoEstoqueService")
class MovimentoEstoqueServiceTest
{
    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentacaoStrategyFactory movimentacaoStrategyFactory;

    @Mock
    private MovimentacaoStrategy movimentacaoStrategy;

    @InjectMocks
    private MovimentoEstoqueService movimentoEstoqueService;

    private Produto produto;
    private MovimentoEstoque movimentoEstoque;
    private MovimentoEstoqueDto movimentoEstoqueDto;

    @BeforeEach
    void setUp()
    {
        produto = new Produto();
        produto.setId(1L);
        produto.setCodigo("P001");
        produto.setDescricao("Produto Teste");
        produto.setTipo(TipoProduto.ELETRONICO);
        produto.setValorFornecedor(new BigDecimal("10.00"));
        produto.setQuantidadeEstoque(100);

        movimentoEstoque = new MovimentoEstoque();
        movimentoEstoque.setId(1L);
        movimentoEstoque.setProduto(produto);
        movimentoEstoque.setTipo(TipoMovimentacao.ENTRADA);
        movimentoEstoque.setQuantidade(50);
        movimentoEstoque.setValorVenda(new BigDecimal("15.00"));
        movimentoEstoque.setDataMovimentacao(LocalDateTime.now());

        movimentoEstoqueDto = new MovimentoEstoqueDto(
                null,
                1L,
                TipoMovimentacao.ENTRADA,
                new BigDecimal("15.00"),
                50,
                null
        );
    }

    @Test
    @DisplayName("Deve registrar movimento com sucesso")
    void registrarMovimento()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenReturn(movimentoEstoque);

        doNothing().when(movimentacaoStrategy).movimentar(any(Produto.class), any(MovimentoEstoque.class));

        MovimentoEstoqueDto result = movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        assertNotNull(result);
        assertEquals(movimentoEstoque.getId(), result.id());
        assertEquals(movimentoEstoque.getProduto().getId(), result.produtoId());
        assertEquals(movimentoEstoque.getTipo(), result.tipo());
        assertEquals(movimentoEstoque.getQuantidade(), result.quantidade());
        assertEquals(movimentoEstoque.getValorVenda(), result.valorVenda());
        assertNotNull(result.dataMovimentacao());

        verify(produtoRepository, times(1)).findById(1L);
        verify(movimentacaoStrategyFactory, times(1)).getStrategy(TipoMovimentacao.ENTRADA);
        verify(movimentacaoStrategy, times(1)).movimentar(any(Produto.class), any(MovimentoEstoque.class));
        verify(produtoRepository, times(1)).save(produto);
        verify(movimentoEstoqueRepository, times(1)).save(any(MovimentoEstoque.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não for encontrado ao registrar movimento")
    void registrarMovimento_ProdutoNaoEncontrado()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto)
        );

        assertTrue(exception.getMessage().contains("Produto não encontrado com ID: 1"));

        verify(produtoRepository, times(1)).findById(1L);
        verify(movimentacaoStrategyFactory, never()).getStrategy(any());
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de movimentação for inválido")
    void registrarMovimento_TipoMovimentacaoInvalido()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenThrow(new RuntimeException("Estratégia não encontrada"));

        TipoMovimentacaoInvalidoException exception = assertThrows(
                TipoMovimentacaoInvalidoException.class,
                () -> movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto)
        );

        assertTrue(exception.getMessage().contains("Tipo de movimentação inválido"));

        verify(produtoRepository, times(1)).findById(1L);
        verify(movimentacaoStrategyFactory, times(1)).getStrategy(TipoMovimentacao.ENTRADA);
        verify(movimentoEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve verificar se data de movimentação é definida automaticamente")
    void registrarMovimento_DataMovimentacaoAutomatica()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenReturn(movimentoEstoque);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);

        movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        verify(movimentoEstoqueRepository).save(movimentoCaptor.capture());
        MovimentoEstoque movimentoCapturado = movimentoCaptor.getValue();
        assertNotNull(movimentoCapturado.getDataMovimentacao());
    }

    @Test
    @DisplayName("Deve buscar movimento por ID com sucesso")
    void buscarPorId()
    {
        Long id = 1L;
        when(movimentoEstoqueRepository.findById(id)).thenReturn(Optional.of(movimentoEstoque));

        MovimentoEstoqueDto result = movimentoEstoqueService.buscarPorId(id);

        assertNotNull(result);
        assertEquals(movimentoEstoque.getId(), result.id());
        assertEquals(movimentoEstoque.getProduto().getId(), result.produtoId());
        assertEquals(movimentoEstoque.getTipo(), result.tipo());
        assertEquals(movimentoEstoque.getQuantidade(), result.quantidade());
        assertEquals(movimentoEstoque.getValorVenda(), result.valorVenda());
        assertEquals(movimentoEstoque.getDataMovimentacao(), result.dataMovimentacao());

        verify(movimentoEstoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando movimento não for encontrado por ID")
    void buscarPorId_MovimentoNaoEncontrado()
    {
        Long id = 999L;
        when(movimentoEstoqueRepository.findById(id)).thenReturn(Optional.empty());

        MovimentacaoNaoEncontradaException exception = assertThrows(
                MovimentacaoNaoEncontradaException.class,
                () -> movimentoEstoqueService.buscarPorId(id)
        );

        assertTrue(exception.getMessage().contains("Movimentação não encontrada com ID: " + id));

        verify(movimentoEstoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar movimentos por produto")
    void listarPorProduto()
    {
        Long produtoId = 1L;

        MovimentoEstoque movimento2 = new MovimentoEstoque();
        movimento2.setId(2L);
        movimento2.setProduto(produto);
        movimento2.setTipo(TipoMovimentacao.SAIDA);
        movimento2.setQuantidade(20);
        movimento2.setValorVenda(new BigDecimal("20.00"));
        movimento2.setDataMovimentacao(LocalDateTime.now());

        List<MovimentoEstoque> movimentos = Arrays.asList(movimentoEstoque, movimento2);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        List<CreatedMovimentoEstoqueDto> result = movimentoEstoqueService.listarPorProduto(produtoId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(movimentoEstoque.getId(), result.get(0).id());
        assertEquals(movimentoEstoque.getTipo(), result.get(0).tipo());

        assertEquals(movimento2.getId(), result.get(1).id());
        assertEquals(movimento2.getTipo(), result.get(1).tipo());

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, times(1)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar movimentos de produto inexistente")
    void listarPorProduto_ProdutoNaoEncontrado()
    {
        Long produtoId = 999L;
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> movimentoEstoqueService.listarPorProduto(produtoId)
        );

        assertTrue(exception.getMessage().contains("Produto não encontrado com ID: " + produtoId));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, never()).findByProduto(any());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando produto não possui movimentos")
    void listarPorProduto_SemMovimentos()
    {
        Long produtoId = 1L;
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(List.of());

        List<CreatedMovimentoEstoqueDto> result = movimentoEstoqueService.listarPorProduto(produtoId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, times(1)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve listar todos os movimentos")
    void listarTodos()
    {
        Produto produto2 = new Produto();
        produto2.setId(2L);
        produto2.setCodigo("P002");
        produto2.setDescricao("Produto Teste 2");
        produto2.setTipo(TipoProduto.ELETRONICO);
        produto2.setValorFornecedor(new BigDecimal("5.00"));
        produto2.setQuantidadeEstoque(50);

        MovimentoEstoque movimento2 = new MovimentoEstoque();
        movimento2.setId(2L);
        movimento2.setProduto(produto2);
        movimento2.setTipo(TipoMovimentacao.SAIDA);
        movimento2.setQuantidade(10);
        movimento2.setValorVenda(new BigDecimal("12.00"));
        movimento2.setDataMovimentacao(LocalDateTime.now());

        List<MovimentoEstoque> movimentos = Arrays.asList(movimentoEstoque, movimento2);
        when(movimentoEstoqueRepository.findAll()).thenReturn(movimentos);

        List<CreatedMovimentoEstoqueDto> result = movimentoEstoqueService.listarTodos();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(movimentoEstoque.getId(), result.get(0).id());
        assertEquals(movimentoEstoque.getTipo(), result.get(0).tipo());

        assertEquals(movimento2.getId(), result.get(1).id());
        assertEquals(movimento2.getTipo(), result.get(1).tipo());

        verify(movimentoEstoqueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há movimentos")
    void listarTodos_SemMovimentos()
    {
        when(movimentoEstoqueRepository.findAll()).thenReturn(List.of());

        List<CreatedMovimentoEstoqueDto> result = movimentoEstoqueService.listarTodos();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(movimentoEstoqueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve registrar movimento de saída com sucesso")
    void registrarMovimento_Saida()
    {
        MovimentoEstoqueDto movimentoSaidaDto = new MovimentoEstoqueDto(
                null,
                1L,
                TipoMovimentacao.SAIDA,
                new BigDecimal("25.00"),
                30,
                null
        );

        MovimentoEstoque movimentoSaida = new MovimentoEstoque();
        movimentoSaida.setId(2L);
        movimentoSaida.setProduto(produto);
        movimentoSaida.setTipo(TipoMovimentacao.SAIDA);
        movimentoSaida.setQuantidade(30);
        movimentoSaida.setValorVenda(new BigDecimal("25.00"));
        movimentoSaida.setDataMovimentacao(LocalDateTime.now());

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.SAIDA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenReturn(movimentoSaida);

        doNothing().when(movimentacaoStrategy).movimentar(any(Produto.class), any(MovimentoEstoque.class));

        MovimentoEstoqueDto result = movimentoEstoqueService.registrarMovimento(movimentoSaidaDto);

        assertNotNull(result);
        assertEquals(TipoMovimentacao.SAIDA, result.tipo());
        assertEquals(30, result.quantidade());
        assertEquals(new BigDecimal("25.00"), result.valorVenda());

        verify(movimentacaoStrategyFactory, times(1)).getStrategy(TipoMovimentacao.SAIDA);
        verify(movimentacaoStrategy, times(1)).movimentar(any(Produto.class), any(MovimentoEstoque.class));
    }

    // MÉTODOS PRIVADOS DE TESTE - Para validar métodos privados através dos públicos

    @Test
    @DisplayName("Deve validar conversão de DTO para entidade")
    void converterDtoParaEntidade_ValidacaoCompleta()
    {
        MovimentoEstoqueDto dtoCompletoDto = new MovimentoEstoqueDto(
                5L,
                1L,
                TipoMovimentacao.ENTRADA,
                new BigDecimal("100.00"),
                75,
                LocalDateTime.now()
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        when(movimentoEstoqueRepository.save(movimentoCaptor.capture())).thenReturn(movimentoEstoque);

        movimentoEstoqueService.registrarMovimento(dtoCompletoDto);

        MovimentoEstoque movimentoCapturado = movimentoCaptor.getValue();
        assertEquals(dtoCompletoDto.id(), movimentoCapturado.getId());
        assertEquals(produto, movimentoCapturado.getProduto());
        assertEquals(dtoCompletoDto.tipo(), movimentoCapturado.getTipo());
        assertEquals(dtoCompletoDto.valorVenda(), movimentoCapturado.getValorVenda());
        assertEquals(dtoCompletoDto.quantidade(), movimentoCapturado.getQuantidade());
    }

    @Test
    @DisplayName("Deve validar conversão de entidade para DTO detalhado")
    void converterEntidadeParaDetalhesDto_ValidacaoCompleta()
    {
        when(movimentoEstoqueRepository.findAll()).thenReturn(List.of(movimentoEstoque));

        List<CreatedMovimentoEstoqueDto> result = movimentoEstoqueService.listarTodos();

        assertNotNull(result);
        assertEquals(1, result.size());

        CreatedMovimentoEstoqueDto dto = result.get(0);
        assertEquals(movimentoEstoque.getId(), dto.id());
        assertEquals(movimentoEstoque.getProduto(), dto.produto());
        assertEquals(movimentoEstoque.getTipo(), dto.tipo());
        assertEquals(movimentoEstoque.getValorVenda(), dto.valorVenda());
        assertEquals(movimentoEstoque.getQuantidade(), dto.quantidade());
        assertEquals(movimentoEstoque.getDataMovimentacao(), dto.dataMovimentacao());
    }

    @Test
    @DisplayName("Deve validar busca de produto por ID através de registrar movimento")
    void buscarProdutoPorId_ValidacaoThroughRegistrarMovimento()
    {
        Long produtoIdInexistente = 999L;
        MovimentoEstoqueDto dtoComProdutoInexistente = new MovimentoEstoqueDto(
                null,
                produtoIdInexistente,
                TipoMovimentacao.ENTRADA,
                new BigDecimal("15.00"),
                50,
                null
        );

        when(produtoRepository.findById(produtoIdInexistente)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> movimentoEstoqueService.registrarMovimento(dtoComProdutoInexistente)
        );

        assertTrue(exception.getMessage().contains("Produto não encontrado com ID: " + produtoIdInexistente));
        verify(produtoRepository, times(1)).findById(produtoIdInexistente);
    }

    @Test
    @DisplayName("Deve validar obtenção de estratégia de movimentação através de registro")
    void obterEstrategiaMovimentacao_ValidacaoThroughRegistro()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenReturn(movimentoEstoque);

        movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        verify(movimentacaoStrategyFactory, times(1)).getStrategy(TipoMovimentacao.ENTRADA);
        verify(movimentacaoStrategy, times(1)).movimentar(any(Produto.class), any(MovimentoEstoque.class));
    }

    @Test
    @DisplayName("Deve validar execução da movimentação através da estratégia")
    void executarMovimentacao_ValidacaoThroughRegistro()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenReturn(movimentoEstoque);

        ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);
        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);

        movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        verify(movimentacaoStrategy, times(1))
                .movimentar(produtoCaptor.capture(), movimentoCaptor.capture());

        assertEquals(produto, produtoCaptor.getValue());
        assertNotNull(movimentoCaptor.getValue());
    }

    @Test
    @DisplayName("Deve validar salvamento do produto após movimentação")
    void salvarProduto_ValidacaoThroughRegistro()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(movimentoEstoqueRepository.save(any(MovimentoEstoque.class)))
                .thenReturn(movimentoEstoque);

        ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);

        movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        verify(produtoRepository, times(1)).save(produtoCaptor.capture());
        assertEquals(produto, produtoCaptor.getValue());
    }

    @Test
    @DisplayName("Deve validar definição automática da data de movimentação")
    void definirDataMovimentacao_ValidacaoThroughRegistro()
    {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        when(movimentoEstoqueRepository.save(movimentoCaptor.capture())).thenReturn(movimentoEstoque);

        movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);
        MovimentoEstoque movimentoCapturado = movimentoCaptor.getValue();

        assertNotNull(movimentoCapturado.getDataMovimentacao());
        assertTrue(movimentoCapturado.getDataMovimentacao().isAfter(antes));
        assertTrue(movimentoCapturado.getDataMovimentacao().isBefore(depois));
    }

    @Test
    @DisplayName("Deve validar salvamento do movimento no repositório")
    void salvarMovimento_ValidacaoThroughRegistro()
    {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(movimentacaoStrategyFactory.getStrategy(TipoMovimentacao.ENTRADA))
                .thenReturn(movimentacaoStrategy);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ArgumentCaptor<MovimentoEstoque> movimentoCaptor = ArgumentCaptor.forClass(MovimentoEstoque.class);
        when(movimentoEstoqueRepository.save(movimentoCaptor.capture())).thenReturn(movimentoEstoque);

        movimentoEstoqueService.registrarMovimento(movimentoEstoqueDto);

        verify(movimentoEstoqueRepository, times(1)).save(any(MovimentoEstoque.class));

        MovimentoEstoque movimentoCapturado = movimentoCaptor.getValue();
        assertNotNull(movimentoCapturado);
        assertEquals(produto, movimentoCapturado.getProduto());
        assertEquals(TipoMovimentacao.ENTRADA, movimentoCapturado.getTipo());
        assertEquals(50, movimentoCapturado.getQuantidade());
        assertEquals(new BigDecimal("15.00"), movimentoCapturado.getValorVenda());
        assertNotNull(movimentoCapturado.getDataMovimentacao());
    }
}