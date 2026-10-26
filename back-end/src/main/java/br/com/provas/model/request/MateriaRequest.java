package br.com.provas.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MateriaRequest(
    @NotBlank(message = "O nome da matéria é obrigatório.")
    @Size(max = 120, message = "O nome da matéria deve conter no máximo 120 caracteres.")
    String nome
) {
    public MateriaRequest { nome = nome == null ? null : nome.strip(); }
}
