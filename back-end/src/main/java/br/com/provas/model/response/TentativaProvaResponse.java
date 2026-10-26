package br.com.provas.model.response;
import java.time.Instant;
import java.util.List;
public record TentativaProvaResponse(Long liberacaoId,String titulo,String materia,Instant iniciadaEm,Instant encerraEm,Instant agora,String situacao,boolean enviadaAgora,List<QuestaoExecucaoResponse> questoes,ProgressoProvasResponse progresso) {
 public TentativaProvaResponse(Long liberacaoId,String titulo,String materia,Instant iniciadaEm,Instant encerraEm,Instant agora,String situacao,boolean enviadaAgora,List<QuestaoExecucaoResponse> questoes){this(liberacaoId,titulo,materia,iniciadaEm,encerraEm,agora,situacao,enviadaAgora,questoes,null);}
 public TentativaProvaResponse withProgresso(ProgressoProvasResponse progresso){return new TentativaProvaResponse(liberacaoId,titulo,materia,iniciadaEm,encerraEm,agora,situacao,enviadaAgora,questoes,progresso);}
}
