package br.com.provas.model.request;
import jakarta.validation.constraints.*;
/** Filtros opcionais aplicados antes da contagem e da paginação. */
public record AlunoFiltroRequest(
 @Size(max=120,message="A pesquisa deve ter até 120 caracteres.") String busca,
 @Pattern(regexp="M|T|R|D",message="Informe um status válido.") String status,
 @Positive(message="Informe uma turma válida.") Long turmaId,
 @Pattern(regexp="nome|numero|cgm|status|turma",message="Selecione uma coluna válida para ordenar.") String ordenacao,
 @Pattern(regexp="ASC|DESC",message="Selecione ordenação ASC ou DESC.") String direcao) {
 public AlunoFiltroRequest(String busca,String status,Long turmaId){this(busca,status,turmaId,"nome","ASC");}
 public AlunoFiltroRequest { ordenacao=ordenacao==null||ordenacao.isBlank()?"nome":ordenacao;direcao=direcao==null||direcao.isBlank()?"ASC":direcao.toUpperCase(java.util.Locale.ROOT);busca=busca==null?"":busca.strip();status=status==null||status.isBlank()?null:status; }
}
