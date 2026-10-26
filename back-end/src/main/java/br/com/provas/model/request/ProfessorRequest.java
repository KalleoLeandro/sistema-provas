package br.com.provas.model.request;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;

public record ProfessorRequest(
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve conter até 120 caracteres.") String nome,
    @NotBlank(message = "O login é obrigatório.")
    @Size(min = 3, max = 120, message = "O login deve conter de 3 a 120 caracteres.") String login,
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, max = 128, message = "A senha deve conter de 8 a 128 caracteres.") String senha,
    @NotEmpty(message = "Selecione pelo menos uma matéria.")
    @Size(max = 1000, message = "A lista pode conter até 1000 matérias.")
    List<@NotNull(message = "A matéria não pode ser nula.") @Valid ProfessorMateriaRequest> materias
) {
    public ProfessorRequest {
        nome = nome == null ? null : nome.strip();
        login = login == null ? null : login.strip();
    }
    @Override
    public String toString() { return "ProfessorRequest[credenciais omitidas]"; }
}
