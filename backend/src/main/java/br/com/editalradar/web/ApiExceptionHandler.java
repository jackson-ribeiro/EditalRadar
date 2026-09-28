package br.com.editalradar.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ParametroInvalidoException.class)
    public ProblemDetail parametroInvalido(ParametroInvalidoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problema.setTitle("Parâmetro inválido");
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail erroInesperado(Exception e) {
        log.error("Erro inesperado na API", e);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado.");
        problema.setTitle("Erro interno");
        return problema;
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String parametro = ex instanceof MethodArgumentTypeMismatchException argumento
                ? argumento.getName()
                : ex.getPropertyName();
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + parametro + "': '" + ex.getValue() + "'.");
        problema.setTitle("Parâmetro inválido");
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }
}
