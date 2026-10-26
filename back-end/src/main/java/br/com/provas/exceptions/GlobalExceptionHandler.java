package br.com.provas.exceptions;

import br.com.provas.model.response.CampoErroResponse;
import br.com.provas.model.response.ErroResponse;
import br.com.provas.model.response.ValidacaoErroResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({org.springframework.data.redis.RedisConnectionFailureException.class,org.springframework.data.redis.RedisSystemException.class,org.springframework.dao.QueryTimeoutException.class})
    public ResponseEntity<ErroResponse> handleRedisUnavailable(Exception exception){return ResponseEntity.status(503).body(new ErroResponse("O salvamento está temporariamente indisponível. Mantenha a prova aberta e tente novamente."));}

    @ExceptionHandler(DefaultErrorException.class)
    public ResponseEntity<ErroResponse> handleDefaultError(DefaultErrorException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ErroResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidacaoErroResponse> handleRequestValidation(MethodArgumentNotValidException exception) {
        var erros = new ArrayList<CampoErroResponse>();
        exception.getBindingResult().getFieldErrors().forEach(error -> erros.add(fieldError(error)));
        exception.getBindingResult().getGlobalErrors().forEach(error ->
            erros.add(new CampoErroResponse("request", message(error.getDefaultMessage()))));
        return badRequest(erros);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleMethodValidation(HandlerMethodValidationException exception) {
        if (exception.isForReturnValue()) {
            return ResponseEntity.internalServerError().body(new ErroResponse("Não foi possível processar a resposta."));
        }
        var erros = new ArrayList<CampoErroResponse>();
        for (var result : exception.getParameterValidationResults()) {
            if (result instanceof ParameterErrors fields) {
                fields.getFieldErrors().forEach(error -> erros.add(fieldError(error)));
                fields.getGlobalErrors().forEach(error -> erros.add(new CampoErroResponse("request", message(error.getDefaultMessage()))));
            } else {
                var campo = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error -> erros.add(new CampoErroResponse(
                    campo == null ? "request" : campo, message(error.getDefaultMessage()))));
            }
        }
        return badRequest(erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidacaoErroResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return badRequest(exception.getConstraintViolations().stream().map(error ->
            new CampoErroResponse(error.getPropertyPath().toString(), error.getMessage())).toList());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidacaoErroResponse> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof tools.jackson.core.JacksonException json && !json.getPath().isEmpty()) {
                var campo = json.getPath().stream().map(reference -> reference.getPropertyName() == null
                    ? "[" + reference.getIndex() + "]" : reference.getPropertyName())
                    .collect(java.util.stream.Collectors.joining("."));
                return badRequest(List.of(new CampoErroResponse(campo, "O valor informado possui um tipo inválido.")));
            }
        }
        return badRequest(List.of(new CampoErroResponse("request", "Envie um corpo JSON válido com os tipos de dados esperados.")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidacaoErroResponse> handleInvalidType(MethodArgumentTypeMismatchException exception) {
        return badRequest(List.of(new CampoErroResponse(exception.getName(), "O valor informado possui um tipo inválido.")));
    }

    private CampoErroResponse fieldError(FieldError error) {
        return new CampoErroResponse(error.getField(), message(error.getDefaultMessage()));
    }

    private String message(String mensagem) { return mensagem == null ? "Valor inválido." : mensagem; }

    private ResponseEntity<ValidacaoErroResponse> badRequest(List<CampoErroResponse> erros) {
        var campos = erros.stream().distinct()
            .sorted(Comparator.comparing(CampoErroResponse::campo).thenComparing(CampoErroResponse::mensagem)).toList();
        // Compatível com as telas que já exibem a propriedade mensagem.
        var detalhes = campos.stream().map(error -> error.campo() + ": " + error.mensagem())
            .collect(java.util.stream.Collectors.joining(" "));
        return ResponseEntity.badRequest().body(new ValidacaoErroResponse("Verifique os campos informados. " + detalhes, campos));
    }
}
