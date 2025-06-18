package br.com.product.nextdomtest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException; // Importar esta
import org.springframework.validation.FieldError; // Importar esta
import org.springframework.web.bind.MethodArgumentNotValidException; // Importar esta
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ResponseEntity<?> handleProdutoNaoEncontrado(ProdutoNaoEncontradoException ex)
    {
        return gerarErro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MovimentacaoNaoEncontradaException.class)
    public ResponseEntity<?> handleMovimentacaoNaoEncontrada(MovimentacaoNaoEncontradaException ex)
    {
        return gerarErro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<?> handleEstoqueInsuficiente(EstoqueInsuficienteException ex)
    {
        return gerarErro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- NOVO: Tratamento para erros de validação de @Valid ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Você pode retornar a mensagem genérica + os detalhes por campo
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase()); // "Bad Request"
        body.put("message", "Um ou mais campos estão inválidos."); // Mensagem geral
        body.put("details", errors); // Adiciona os erros por campo

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex)
    {
        String message = "Corpo da requisição inválido ou formato JSON incorreto. ";

        if (ex.getMostSpecificCause() == null)
        {
            message += ex.getMessage();
        }
        message += ex.getMostSpecificCause().getMessage();

        return gerarErro(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex)
    {
        return gerarErro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + ex.getMessage());
    }

    @ExceptionHandler(TipoMovimentacaoInvalidoException.class)
    public ResponseEntity<?> handleTipoMovimentacaoInvalido(TipoMovimentacaoInvalidoException ex)
    {
        return gerarErro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<?> handleOperacaoNaoPermitida(OperacaoNaoPermitidaException ex)
    {
        return gerarErro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> gerarErro(HttpStatus status, String mensagem)
    {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensagem);
        return new ResponseEntity<>(body, status);
    }
}