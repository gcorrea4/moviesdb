package fiap.com.br.moviedb.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Transforma erros de entrada do cliente em resposta 400 (Bad Request) com uma
 * mensagem, em vez de deixar estourar um erro 500 (erro do servidor).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // ex: sort=abc, yearFrom > yearTo
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    // ex: yearFrom=abc (texto onde deveria ser número)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> wrongType(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Valor inválido para o parâmetro '" + e.getName() + "'"));
    }
}
