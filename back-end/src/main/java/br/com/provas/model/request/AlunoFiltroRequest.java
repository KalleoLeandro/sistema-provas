package br.com.provas.model.request;
import jakarta.validation.constraints.*;
/** Filtros opcionais aplicados antes da contagem e da paginação. */
public record AlunoFiltroRequest(
 @Size(max=120,message="A pesquisa deve ter até 120 caracteres.") String busca,
 @Pattern(regexp="M|T|R|D",message="Informe um status válido.") String status,
 @Positive(message="Informe uma turma válida.") Long turmaId) {
 public AlunoFiltroRequest { busca=busca==null?"":busca.strip();status=status==null||status.isBlank()?null:status; }
}
