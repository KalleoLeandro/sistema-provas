package br.com.provas.exceptions;

import java.util.Objects;
import org.springframework.http.HttpStatus;

public class DefaultErrorException extends RuntimeException {
	
    private static final long serialVersionUID = 1L;
	private final HttpStatus status;

    public DefaultErrorException(String mensagem) {
        this(mensagem, HttpStatus.BAD_REQUEST);
    }

    public DefaultErrorException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = Objects.requireNonNull(status);
        if (!status.isError()) {
            throw new IllegalArgumentException("O status deve representar um erro HTTP.");
        }
    }

    public HttpStatus getStatus() {
        return status;
    }
}
