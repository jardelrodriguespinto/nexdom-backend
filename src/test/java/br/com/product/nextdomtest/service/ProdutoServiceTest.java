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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes da classe ProdutoService")
class ProdutoServiceTest
{
    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private ProdutoDto produtoDto;
    private MovimentoEstoque movimentoSaida;
    private MovimentoEstoque movimentoEntrada;

    @BeforeEach
    void setUp()
    {
        produto = new Produto();
        produto.setId(1L);
        produto.setCodigo("P001");
        produto.setDescricao("Produto Teste");
        produto.setTipo(TipoProduto.ELETRONICO);
        produto.setValorFornecedor(new BigDecimal("100.00")); // Valor total para 10 unidades
        produto.setQuantidadeEstoque(100);

        produtoDto = new ProdutoDto(
                1L,
                "P001",
                "Produto Teste",
                TipoProduto.ELETRONICO,
                new BigDecimal("100.00"),
                100
        );

        movimentoSaida = new MovimentoEstoque();
        movimentoSaida.setId(1L);
        movimentoSaida.setProduto(produto);
        movimentoSaida.setTipo(TipoMovimentacao.SAIDA);
        movimentoSaida.setQuantidade(5);
        movimentoSaida.setValorVenda(new BigDecimal("15.00"));

        movimentoEntrada = new MovimentoEstoque();
        movimentoEntrada.setId(2L);
        movimentoEntrada.setProduto(produto);
        movimentoEntrada.setTipo(TipoMovimentacao.ENTRADA);
        movimentoEntrada.setQuantidade(10);
    }

    @Test
    @DisplayName("Deve salvar produto com sucesso")
    void salvar()
    {
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ProdutoDto result = produtoService.salvar(produtoDto);

        assertNotNull(result);
        assertEquals(produtoDto.id(), result.id());
        assertEquals(produtoDto.codigo(), result.codigo());
        assertEquals(produtoDto.descricao(), result.descricao());
        assertEquals(produtoDto.tipo(), result.tipo());
        assertEquals(produtoDto.valorFornecedor(), result.valorFornecedor());
        assertEquals(produtoDto.quantidadeEstoque(), result.quantidadeEstoque());

        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve salvar produto com ID nulo")
    void salvar_ComIdNulo()
    {
        ProdutoDto produtoSemId = new ProdutoDto(
                null,
                "P002",
                "Produto Novo",
                TipoProduto.ELETRONICO,
                new BigDecimal("50.00"),
                25
        );

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId(2L);
        produtoSalvo.setCodigo("P002");
        produtoSalvo.setDescricao("Produto Novo");
        produtoSalvo.setTipo(TipoProduto.ELETRONICO);
        produtoSalvo.setValorFornecedor(new BigDecimal("50.00"));
        produtoSalvo.setQuantidadeEstoque(25);

        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);

        ProdutoDto result = produtoService.salvar(produtoSemId);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals(produtoSemId.codigo(), result.codigo());
        assertEquals(produtoSemId.descricao(), result.descricao());

        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void buscarPorId()
    {
        Long id = 1L;
        when(produtoRepository.findById(id)).thenReturn(Optional.of(produto));

        ProdutoDto result = produtoService.buscarPorId(id);

        assertNotNull(result);
        assertEquals(produto.getId(), result.id());
        assertEquals(produto.getCodigo(), result.codigo());
        assertEquals(produto.getDescricao(), result.descricao());
        assertEquals(produto.getTipo(), result.tipo());
        assertEquals(produto.getValorFornecedor(), result.valorFornecedor());
        assertEquals(produto.getQuantidadeEstoque(), result.quantidadeEstoque());

        verify(produtoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não for encontrado por ID")
    void buscarPorId_ProdutoNaoEncontrado()
    {
        Long id = 999L;
        when(produtoRepository.findById(id)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> produtoService.buscarPorId(id)
        );

        verify(produtoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void listarTodos()
    {
        Produto produto2 = new Produto();
        produto2.setId(2L);
        produto2.setCodigo("P002");
        produto2.setDescricao("Produto Teste 2");
        produto2.setTipo(TipoProduto.ELETRONICO);
        produto2.setValorFornecedor(new BigDecimal("5.00"));
        produto2.setQuantidadeEstoque(50);

        List<Produto> produtos = Arrays.asList(produto, produto2);
        when(produtoRepository.findAll()).thenReturn(produtos);

        List<ProdutoDto> result = produtoService.listarTodos();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(produto.getId(), result.get(0).id());
        assertEquals(produto.getCodigo(), result.get(0).codigo());

        assertEquals(produto2.getId(), result.get(1).id());
        assertEquals(produto2.getCodigo(), result.get(1).codigo());

        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void listarTodos_ListaVazia()
    {
        when(produtoRepository.findAll()).thenReturn(List.of());

        List<ProdutoDto> result = produtoService.listarTodos();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar produtos por tipo")
    void listarPorTipo()
    {
        TipoProduto tipo = TipoProduto.ELETRONICO;
        List<Produto> produtos = List.of(produto);
        when(produtoRepository.findByTipo(tipo)).thenReturn(produtos);

        List<ProdutoDto> result = produtoService.listarPorTipo(tipo);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(produto.getId(), result.get(0).id());
        assertEquals(produto.getTipo(), result.get(0).tipo());

        verify(produtoRepository, times(1)).findByTipo(tipo);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos do tipo especificado")
    void listarPorTipo_SemProdutos()
    {
        TipoProduto tipo = TipoProduto.ELETRONICO;
        when(produtoRepository.findByTipo(tipo)).thenReturn(List.of());

        List<ProdutoDto> result = produtoService.listarPorTipo(tipo);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(produtoRepository, times(1)).findByTipo(tipo);
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void atualizar()
    {
        Long id = 1L;
        ProdutoDto produtoAtualizado = new ProdutoDto(
                null,
                "P001-UPD",
                "Produto Atualizado",
                TipoProduto.ELETRONICO,
                new BigDecimal("12.00"),
                150
        );

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId(id);
        produtoSalvo.setCodigo(produtoAtualizado.codigo());
        produtoSalvo.setDescricao(produtoAtualizado.descricao());
        produtoSalvo.setTipo(produtoAtualizado.tipo());
        produtoSalvo.setValorFornecedor(produtoAtualizado.valorFornecedor());
        produtoSalvo.setQuantidadeEstoque(produtoAtualizado.quantidadeEstoque());

        when(produtoRepository.existsById(id)).thenReturn(true);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);

        ProdutoDto result = produtoService.atualizar(id, produtoAtualizado);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals(produtoAtualizado.codigo(), result.codigo());
        assertEquals(produtoAtualizado.descricao(), result.descricao());
        assertEquals(produtoAtualizado.tipo(), result.tipo());
        assertEquals(produtoAtualizado.valorFornecedor(), result.valorFornecedor());
        assertEquals(produtoAtualizado.quantidadeEstoque(), result.quantidadeEstoque());

        verify(produtoRepository, times(1)).existsById(id);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar produto inexistente")
    void atualizar_ProdutoNaoEncontrado()
    {
        Long id = 999L;
        when(produtoRepository.existsById(id)).thenReturn(false);

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> produtoService.atualizar(id, produtoDto)
        );

        verify(produtoRepository, times(1)).existsById(id);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve deletar produto com sucesso")
    void deletar()
    {
        Long id = 1L;
        when(produtoRepository.existsById(id)).thenReturn(true);
        when(movimentoEstoqueRepository.existsByProdutoId(id)).thenReturn(false);
        doNothing().when(produtoRepository).deleteById(id);

        assertDoesNotThrow(() -> produtoService.deletar(id));

        verify(produtoRepository, times(1)).existsById(id);
        verify(movimentoEstoqueRepository, times(1)).existsByProdutoId(id);
        verify(produtoRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar produto inexistente")
    void deletar_ProdutoNaoEncontrado()
    {
        Long id = 999L;
        when(produtoRepository.existsById(id)).thenReturn(false);

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> produtoService.deletar(id)
        );

        verify(produtoRepository, times(1)).existsById(id);
        verify(produtoRepository, never()).deleteById(any());
        verify(movimentoEstoqueRepository, never()).existsByProdutoId(any());
    }

    @Test
    @DisplayName("Deve consultar lucro do produto com sucesso")
    void consultarLucro()
    {
        Long produtoId = 1L;

        MovimentoEstoque movimento1 = new MovimentoEstoque();
        movimento1.setId(1L);
        movimento1.setProduto(produto);
        movimento1.setTipo(TipoMovimentacao.SAIDA);
        movimento1.setQuantidade(3);
        movimento1.setValorVenda(new BigDecimal("15.00"));

        MovimentoEstoque movimento2 = new MovimentoEstoque();
        movimento2.setId(2L);
        movimento2.setProduto(produto);
        movimento2.setTipo(TipoMovimentacao.SAIDA);
        movimento2.setQuantidade(2);
        movimento2.setValorVenda(new BigDecimal("18.00"));

        MovimentoEstoque movimentoEntrada = new MovimentoEstoque();
        movimentoEntrada.setId(3L);
        movimentoEntrada.setProduto(produto);
        movimentoEntrada.setTipo(TipoMovimentacao.ENTRADA);
        movimentoEntrada.setQuantidade(10);

        List<MovimentoEstoque> movimentos = Arrays.asList(movimento1, movimento2, movimentoEntrada);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);
        assertEquals(produto.getDescricao(), result.descricao());

        // Verificar quantidade de saída: 3 + 2 = 5
        assertEquals(5, result.quantidadeSaida());

        // Verificar se o lucro foi calculado corretamente
        // Valor compra unitário = 100.00 / 10 = 10.00
        // Movimento1: (15.00 - 10.00) * 3 = 15.00
        // Movimento2: (18.00 - 10.00) * 2 = 16.00
        // Total: 15.00 + 16.00 = 31.00
        BigDecimal lucroEsperado = new BigDecimal("31.00");
        assertEquals(0, lucroEsperado.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve consultar lucro zero quando não há movimentos de saída")
    void consultarLucro_SemMovimentosSaida()
    {
        Long produtoId = 1L;

        MovimentoEstoque movimentoEntrada = new MovimentoEstoque();
        movimentoEntrada.setId(1L);
        movimentoEntrada.setProduto(produto);
        movimentoEntrada.setTipo(TipoMovimentacao.ENTRADA);
        movimentoEntrada.setQuantidade(10);

        List<MovimentoEstoque> movimentos = List.of(movimentoEntrada);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);
        assertEquals(0, result.quantidadeSaida());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve lançar exceção ao consultar lucro de produto inexistente")
    void consultarLucro_ProdutoNaoEncontrado()
    {
        Long produtoId = 999L;
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

        ProdutoNaoEncontradoException exception = assertThrows(
                ProdutoNaoEncontradoException.class,
                () -> produtoService.consultarLucro(produtoId)
        );

        assertTrue(exception.getMessage().contains("Produto não encontrado com ID: " + produtoId));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, never()).findByProduto(any());
    }

    @Test
    @DisplayName("Deve calcular valor de venda unitário corretamente")
    void consultarLucro_ValorVendaUnitarioCorreto()
    {
        Long produtoId = 1L;

        // Criar movimentos com valores diferentes
        MovimentoEstoque venda1 = new MovimentoEstoque();
        venda1.setId(1L);
        venda1.setProduto(produto);
        venda1.setTipo(TipoMovimentacao.SAIDA);
        venda1.setQuantidade(2);
        venda1.setValorVenda(new BigDecimal("20.00"));

        MovimentoEstoque venda2 = new MovimentoEstoque();
        venda2.setId(2L);
        venda2.setProduto(produto);
        venda2.setTipo(TipoMovimentacao.SAIDA);
        venda2.setQuantidade(3);
        venda2.setValorVenda(new BigDecimal("25.00"));

        MovimentoEstoque entrada = new MovimentoEstoque();
        entrada.setId(3L);
        entrada.setProduto(produto);
        entrada.setTipo(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(10);

        List<MovimentoEstoque> movimentos = Arrays.asList(venda1, venda2, entrada);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Valor médio de venda: ((20.00 * 2) + (25.00 * 3)) / (2 + 3) = 115.00 / 5 = 23.00
        BigDecimal valorVendaEsperado = new BigDecimal("23.00");
        assertEquals(0, valorVendaEsperado.compareTo(result.valorVendaUnitario()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve retornar lucro unitário zero quando valor venda é menor que compra")
    void consultarLucro_LucroUnitarioZeroQuandoVendaMenorQueCompra()
    {
        Long produtoId = 1L;

        // Criar produto com valor alto
        Produto produtoCarol = new Produto();
        produtoCarol.setId(produtoId);
        produtoCarol.setCodigo("P999");
        produtoCarol.setDescricao("Produto Caro");
        produtoCarol.setTipo(TipoProduto.ELETRONICO);
        produtoCarol.setValorFornecedor(new BigDecimal("1000.00")); // R$ 100.00 por unidade (10 unidades)
        produtoCarol.setQuantidadeEstoque(10);

        MovimentoEstoque vendaBaixa = new MovimentoEstoque();
        vendaBaixa.setId(1L);
        vendaBaixa.setProduto(produtoCarol);
        vendaBaixa.setTipo(TipoMovimentacao.SAIDA);
        vendaBaixa.setQuantidade(1);
        vendaBaixa.setValorVenda(new BigDecimal("50.00")); // Vendendo por menos que custou

        MovimentoEstoque entrada = new MovimentoEstoque();
        entrada.setId(2L);
        entrada.setProduto(produtoCarol);
        entrada.setTipo(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(10);

        List<MovimentoEstoque> movimentos = Arrays.asList(vendaBaixa, entrada);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produtoCarol));
        when(movimentoEstoqueRepository.findByProduto(produtoCarol)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Lucro unitário deve ser zero quando venda < compra
        assertEquals(0, BigDecimal.ZERO.compareTo(result.lucroUnitario()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produtoCarol);
    }

    @Test
    @DisplayName("Deve calcular corretamente quando não há movimento de entrada")
    void consultarLucro_SemMovimentoEntrada()
    {
        Long produtoId = 1L;

        MovimentoEstoque venda = new MovimentoEstoque();
        venda.setId(1L);
        venda.setProduto(produto);
        venda.setTipo(TipoMovimentacao.SAIDA);
        venda.setQuantidade(2);
        venda.setValorVenda(new BigDecimal("150.00"));

        List<MovimentoEstoque> movimentos = List.of(venda);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Quando não há movimento de entrada, assume que valorFornecedor já é unitário
        // Lucro = (150.00 - 100.00) * 2 = 100.00
        BigDecimal lucroEsperado = new BigDecimal("100.00");
        assertEquals(0, lucroEsperado.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve calcular lucro com múltiplas entradas")
    void consultarLucro_MultiplasEntradas()
    {
        Long produtoId = 1L;

        MovimentoEstoque entrada1 = new MovimentoEstoque();
        entrada1.setId(1L);
        entrada1.setProduto(produto);
        entrada1.setTipo(TipoMovimentacao.ENTRADA);
        entrada1.setQuantidade(10);

        MovimentoEstoque entrada2 = new MovimentoEstoque();
        entrada2.setId(2L);
        entrada2.setProduto(produto);
        entrada2.setTipo(TipoMovimentacao.ENTRADA);
        entrada2.setQuantidade(5);

        MovimentoEstoque saida = new MovimentoEstoque();
        saida.setId(3L);
        saida.setProduto(produto);
        saida.setTipo(TipoMovimentacao.SAIDA);
        saida.setQuantidade(3);
        saida.setValorVenda(new BigDecimal("20.00"));

        List<MovimentoEstoque> movimentos = Arrays.asList(entrada1, entrada2, saida);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Deve usar a primeira entrada para calcular o valor unitário
        // Valor unitário = 100.00 / 10 = 10.00
        // Lucro = (20.00 - 10.00) * 3 = 30.00
        BigDecimal lucroEsperado = new BigDecimal("30.00");
        assertEquals(0, lucroEsperado.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve calcular valor compra unitário quando não há entrada")
    void consultarLucro_ValorCompraUnitarioSemEntrada()
    {
        Long produtoId = 1L;

        MovimentoEstoque saida = new MovimentoEstoque();
        saida.setId(1L);
        saida.setProduto(produto);
        saida.setTipo(TipoMovimentacao.SAIDA);
        saida.setQuantidade(2);
        saida.setValorVenda(new BigDecimal("120.00"));

        List<MovimentoEstoque> movimentos = List.of(saida);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Quando não há entrada, deve usar o valor do fornecedor como unitário
        assertEquals(0, produto.getValorFornecedor().compareTo(result.valorCompraUnitario()));

        // Lucro = (120.00 - 100.00) * 2 = 40.00
        BigDecimal lucroEsperado = new BigDecimal("40.00");
        assertEquals(0, lucroEsperado.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve retornar valores zero quando produto não tem movimentações")
    void consultarLucro_SemMovimentacoes()
    {
        Long produtoId = 1L;

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(List.of());

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);
        assertEquals(produto.getDescricao(), result.descricao());
        assertEquals(0, result.quantidadeSaida());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.valorVendaUnitario()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.lucroUnitario()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve calcular lucro com múltiplas saídas com valores diferentes")
    void consultarLucro_MultiplasSaidasValoresDiferentes()
    {
        Long produtoId = 1L;

        MovimentoEstoque entrada = new MovimentoEstoque();
        entrada.setId(1L);
        entrada.setProduto(produto);
        entrada.setTipo(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(20);

        MovimentoEstoque saida1 = new MovimentoEstoque();
        saida1.setId(2L);
        saida1.setProduto(produto);
        saida1.setTipo(TipoMovimentacao.SAIDA);
        saida1.setQuantidade(5);
        saida1.setValorVenda(new BigDecimal("12.00"));

        MovimentoEstoque saida2 = new MovimentoEstoque();
        saida2.setId(3L);
        saida2.setProduto(produto);
        saida2.setTipo(TipoMovimentacao.SAIDA);
        saida2.setQuantidade(3);
        saida2.setValorVenda(new BigDecimal("15.00"));

        MovimentoEstoque saida3 = new MovimentoEstoque();
        saida3.setId(4L);
        saida3.setProduto(produto);
        saida3.setTipo(TipoMovimentacao.SAIDA);
        saida3.setQuantidade(2);
        saida3.setValorVenda(new BigDecimal("18.00"));

        List<MovimentoEstoque> movimentos = Arrays.asList(entrada, saida1, saida2, saida3);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Quantidade total de saída: 5 + 3 + 2 = 10
        assertEquals(10, result.quantidadeSaida());

        // Valor compra unitário: 100.00 / 20 = 5.00
        BigDecimal valorCompraEsperado = new BigDecimal("5.00");
        assertEquals(0, valorCompraEsperado.compareTo(result.valorCompraUnitario()));

        // Valor venda médio: ((12.00 * 5) + (15.00 * 3) + (18.00 * 2)) / 10 = 141.00 / 10 = 14.10
        BigDecimal valorVendaEsperado = new BigDecimal("14.10");
        assertEquals(0, valorVendaEsperado.compareTo(result.valorVendaUnitario()));

        // Lucro unitário: 14.10 - 5.00 = 9.10
        BigDecimal lucroUnitarioEsperado = new BigDecimal("9.10");
        assertEquals(0, lucroUnitarioEsperado.compareTo(result.lucroUnitario()));

        // Lucro total: (12.00-5.00)*5 + (15.00-5.00)*3 + (18.00-5.00)*2 = 35 + 30 + 26 = 91.00
        BigDecimal lucroTotalEsperado = new BigDecimal("91.00");
        assertEquals(0, lucroTotalEsperado.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve calcular lucro com valor de venda igual ao de compra")
    void consultarLucro_ValorVendaIgualCompra()
    {
        Long produtoId = 1L;

        MovimentoEstoque entrada = new MovimentoEstoque();
        entrada.setId(1L);
        entrada.setProduto(produto);
        entrada.setTipo(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(10);

        MovimentoEstoque saida = new MovimentoEstoque();
        saida.setId(2L);
        saida.setProduto(produto);
        saida.setTipo(TipoMovimentacao.SAIDA);
        saida.setQuantidade(5);
        saida.setValorVenda(new BigDecimal("10.00")); // Mesmo valor da compra unitária

        List<MovimentoEstoque> movimentos = Arrays.asList(entrada, saida);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);

        // Valor compra unitário: 100.00 / 10 = 10.00
        BigDecimal valorCompraEsperado = new BigDecimal("10.00");
        assertEquals(0, valorCompraEsperado.compareTo(result.valorCompraUnitario()));

        // Valor venda unitário: 10.00
        BigDecimal valorVendaEsperado = new BigDecimal("10.00");
        assertEquals(0, valorVendaEsperado.compareTo(result.valorVendaUnitario()));

        // Lucro unitário: 10.00 - 10.00 = 0.00
        assertEquals(0, BigDecimal.ZERO.compareTo(result.lucroUnitario()));

        // Lucro total: 0.00 * 5 = 0.00
        assertEquals(0, BigDecimal.ZERO.compareTo(result.lucroTotal()));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }

    @Test
    @DisplayName("Deve validar se produto existe antes de consultar lucro")
    void consultarLucro_ValidarExistenciaProduto()
    {
        Long produtoId = 1L;

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(List.of());

        assertDoesNotThrow(() -> produtoService.consultarLucro(produtoId));

        verify(produtoRepository, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve retornar DTO de lucro com todos os campos preenchidos")
    void consultarLucro_DtoCompleto()
    {
        Long produtoId = 1L;

        MovimentoEstoque entrada = new MovimentoEstoque();
        entrada.setId(1L);
        entrada.setProduto(produto);
        entrada.setTipo(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(10);

        MovimentoEstoque saida = new MovimentoEstoque();
        saida.setId(2L);
        saida.setProduto(produto);
        saida.setTipo(TipoMovimentacao.SAIDA);
        saida.setQuantidade(3);
        saida.setValorVenda(new BigDecimal("20.00"));

        List<MovimentoEstoque> movimentos = Arrays.asList(entrada, saida);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(movimentoEstoqueRepository.findByProduto(produto)).thenReturn(movimentos);

        LucroProdutoDto result = produtoService.consultarLucro(produtoId);

        assertNotNull(result);
        assertNotNull(result.produto());
        assertNotNull(result.descricao());
        assertNotNull(result.quantidadeSaida());
        assertNotNull(result.valorCompraUnitario());
        assertNotNull(result.valorVendaUnitario());
        assertNotNull(result.lucroUnitario());
        assertNotNull(result.lucroTotal());

        assertEquals(produto, result.produto());
        assertEquals(produto.getDescricao(), result.descricao());

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(movimentoEstoqueRepository, atLeast(2)).findByProduto(produto);
    }
}