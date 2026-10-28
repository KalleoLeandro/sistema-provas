package br.com.provas.model.request;
import jakarta.validation.constraints.Pattern;
/** Coluna e direção opcionais; cada consulta permite apenas suas próprias colunas. */
public record OrdenacaoRequest(
 @Pattern(regexp="aluno|titulo|materia|professor|turma|disponivelEm|encerraEm|situacao|entregueEm|nota",message="Selecione uma coluna válida para ordenar.") String ordenacao,
 @Pattern(regexp="ASC|DESC",message="Selecione ordenação ASC ou DESC.") String direcao){
 public OrdenacaoRequest{ordenacao=ordenacao==null||ordenacao.isBlank()?null:ordenacao;direcao=direcao==null||direcao.isBlank()?"ASC":direcao.toUpperCase(java.util.Locale.ROOT);}
}
