package br.com.product.nextdomtest.controller;

import br.com.product.nextdomtest.dto.CreatedMovimentoEstoqueDto;
import br.com.product.nextdomtest.dto.MovimentoEstoqueDto;
import br.com.product.nextdomtest.service.MovimentoEstoqueService;
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
@RequestMapping("/api/movimentos")
@Tag(name = "Movimento de Estoque", description = "Operações de movimentação de estoque (entrada/saída)")
public class MovimentoEstoqueController
{
    private final MovimentoEstoqueService movimentoService;

    @Autowired
    public MovimentoEstoqueController(MovimentoEstoqueService movimentoService)
    {
        this.movimentoService = movimentoService;
    }

    @PostMapping
    @Operation(
            summary = "Registrar movimento de estoque",
            description = "Registra uma nova movimentação de estoque (entrada ou saída) para um produto"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimento registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovimentoEstoqueDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MovimentoEstoqueDto> registrarMovimento(@Valid @RequestBody MovimentoEstoqueDto dto)
    {
        MovimentoEstoqueDto movimentoSalvo = movimentoService.registrarMovimento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimentoSalvo);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar movimento por ID",
            description = "Retorna os detalhes de um movimento de estoque específico pelo seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MovimentoEstoqueDto.class))),
            @ApiResponse(responseCode = "404", description = "Movimento não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<MovimentoEstoqueDto> buscarPorId(
            @Parameter(description = "ID do movimento de estoque", required = true, example = "1")
            @PathVariable Long id)
    {
        MovimentoEstoqueDto movimento = movimentoService.buscarPorId(id);
        return ResponseEntity.ok(movimento);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os movimentos",
            description = "Retorna uma lista com todos os movimentos de estoque registrados no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentos retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CreatedMovimentoEstoqueDto.class)))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CreatedMovimentoEstoqueDto>> listarTodos()
    {
        List<CreatedMovimentoEstoqueDto> movimentos = movimentoService.listarTodos();
        return ResponseEntity.ok(movimentos);
    }

    @GetMapping("/produto/{produtoId}")
    @Operation(
            summary = "Listar movimentos por produto",
            description = "Retorna todos os movimentos de estoque relacionados a um produto específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de movimentos do produto retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CreatedMovimentoEstoqueDto.class)))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CreatedMovimentoEstoqueDto>> listarPorProduto(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long produtoId)
    {
        List<CreatedMovimentoEstoqueDto> movimentos = movimentoService.listarPorProduto(produtoId);
        return ResponseEntity.ok(movimentos);
    }
}