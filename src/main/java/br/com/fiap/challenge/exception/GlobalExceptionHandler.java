package br.com.fiap.challenge.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErroResponse> erro(
            HttpStatus status, String message, Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(Instant.now(), status.value(), message, fields));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST, "Dados invalidos", fields);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ErroResponse> requisicaoInvalida(Exception ex) {
        return erro(
                HttpStatus.BAD_REQUEST,
                "Confira o JSON, os tipos e os parametros enviados",
                Map.of());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErroResponse> persistencia(DataAccessException ex) {
        log.error("Falha ao acessar o banco", ex);
        return erro(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Nao foi possivel concluir a operacao no banco de dados",
                Map.of());
    }
}
