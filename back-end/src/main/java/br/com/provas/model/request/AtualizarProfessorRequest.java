package br.com.provas.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record AtualizarProfessorRequest(
    @NotBlank(message="O nome é obrigatório.") @Size(max=120,message="O nome deve conter até 120 caracteres.") String nome,
    @NotBlank(message="O login é obrigatório.") @Size(min=3,max=120,message="O login deve conter de 3 a 120 caracteres.") String login,
    @Size(min=8,max=128,message="A nova senha deve conter de 8 a 128 caracteres.") String senha,
    @NotEmpty(message="Selecione pelo menos uma matéria.") @Size(max=1000,message="Selecione até 1000 matérias.")
    List<@NotNull(message="A matéria não pode ser nula.") @Valid ProfessorMateriaRequest> materias
) {
    public AtualizarProfessorRequest{nome=nome==null?null:nome.strip();login=login==null?null:login.strip();senha=senha==null||senha.isBlank()?null:senha;}
    @Override public String toString(){return "AtualizarProfessorRequest[credenciais omitidas]";}
}
