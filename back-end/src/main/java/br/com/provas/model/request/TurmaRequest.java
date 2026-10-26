package br.com.provas.model.request;

import java.util.Locale;
import jakarta.validation.constraints.*;

public record TurmaRequest(
    @NotBlank(message = "O ensino é obrigatório.")
    @Pattern(regexp = "F|M", message = "Selecione ensino fundamental ou médio.")
    String ensino,
    @NotNull(message = "O ano é obrigatório.")
    @Min(value = 1, message = "O ano deve estar entre 1 e 9.")
    @Max(value = 9, message = "O ano deve estar entre 1 e 9.")
    Integer ano,
    @NotBlank(message = "A letra é obrigatória.")
    @Pattern(regexp = "[A-Z]", message = "Informe uma única letra de A a Z.")
    String letra
) {
    public TurmaRequest {
        ensino = ensino == null ? null : ensino.strip().toUpperCase(Locale.ROOT);
        letra = letra == null ? null : letra.strip().toUpperCase(Locale.ROOT);
    }
}
