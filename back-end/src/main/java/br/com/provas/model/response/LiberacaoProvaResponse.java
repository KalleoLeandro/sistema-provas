package br.com.provas.model.response;
import java.time.Instant;
public record LiberacaoProvaResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,String situacao,Integer duracaoMinutos,boolean tokenConfigurado) {}
