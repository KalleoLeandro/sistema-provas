package br.com.provas.model.response;
import java.time.Instant;
public record LiberacaoProvaResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,String situacao,Integer duracaoMinutos,boolean tokenConfigurado, java.util.List<AssuntoResponse> assuntos) {
 public LiberacaoProvaResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,String situacao,Integer duracaoMinutos,boolean tokenConfigurado){this(id,titulo,materiaId,materia,disponivelEm,encerraEm,situacao,duracaoMinutos,tokenConfigurado,java.util.List.of());}
}
