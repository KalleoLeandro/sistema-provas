package br.com.provas.model.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record ProfessorMateriaRequest(
    @NotNull(message = "A matéria é obrigatória.")
    @Positive(message = "O identificador da matéria deve ser positivo.") Long materiaId,
    @NotNull(message = "Informe a lista de turmas da matéria, mesmo quando vazia.")
    @Size(max = 468, message = "A lista pode conter até 468 turmas.")
    List<@NotNull(message = "A turma não pode ser nula.") @Positive(message = "O identificador da turma deve ser positivo.") Long> turmas
) {}
