package br.com.product.nextdomtest.controller;

import br.com.product.nextdomtest.dto.ProdutoDto;
import br.com.product.nextdomtest.dto.LucroProdutoDto;
import br.com.product.nextdomtest.enums.TipoProduto;
import br.com.product.nextdomtest.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Operações de gerenciamento de produtos e consultas de lucro")
public class ProdutoController
{
    private final ProdutoService produtoService;

    @Autowired
    public ProdutoController(ProdutoService produtoService)
    {
        this.produtoService = produtoService;
    }

    @PostMapping
    @Operation(
            summary = "Criar novo produto",
            description = "Cadastra um novo produto no sistema com suas informações básicas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Produto já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProdutoDto> criarProduto(@Valid @RequestBody ProdutoDto dto)
    {
        ProdutoDto produtoSalvo = produtoService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna os detalhes de um produto específico pelo seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoDto.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProdutoDto> buscarPorId(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long id)
    {
        ProdutoDto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os produtos",
            description = "Retorna uma lista com todos os produtos cadastrados no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProdutoDto.class)))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ProdutoDto>> listarTodos()
    {
        List<ProdutoDto> produtos = produtoService.listarTodos();
        return ResponseEntity.ok(produtos);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza as informações de um produto existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProdutoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProdutoDto> atualizarProduto(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProdutoDto dto)
    {
        ProdutoDto produtoAtualizado = produtoService.atualizar(id, dto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar produto",
            description = "Remove um produto do sistema permanentemente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto possui movimentações e não pode ser deletado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> deletarProduto(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long id)
    {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(
            summary = "Buscar produtos por tipo",
            description = "Retorna todos os produtos de um tipo específico com suas quantidades em estoque"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos por tipo retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProdutoDto.class)))),
            @ApiResponse(responseCode = "400", description = "Tipo de produto inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<ProdutoDto>> buscarPorTipo(
            @Parameter(description = "Tipo do produto", required = true,
                    schema = @Schema(implementation = TipoProduto.class))
            @PathVariable TipoProduto tipo)
    {
        List<ProdutoDto> produtos = produtoService.listarPorTipo(tipo);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}/lucro")
    @Operation(
            summary = "Consultar lucro do produto",
            description = "Retorna informações detalhadas sobre o lucro de um produto específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados de lucro retornados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LucroProdutoDto.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<LucroProdutoDto> consultarLucro(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long id)
    {
        LucroProdutoDto lucro = produtoService.consultarLucro(id);
        return ResponseEntity.ok(lucro);
    }
}