package br.com.provas.model.response;
import java.time.Instant;
public record ProvaAlunoResponse(Long id,String titulo,String materia,String professor,Instant disponivelEm,Instant encerraEm,String situacao,Integer duracaoMinutos,boolean tokenConfigurado) {}
