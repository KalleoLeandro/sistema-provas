package br.com.provas.model.response;
import java.time.Instant;
import java.util.List;
public record TentativaProvaResponse(Long liberacaoId,String titulo,String materia,Instant iniciadaEm,Instant encerraEm,Instant agora,String situacao,boolean enviadaAgora,List<QuestaoExecucaoResponse> questoes) {}
