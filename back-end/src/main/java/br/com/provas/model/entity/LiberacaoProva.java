package br.com.provas.model.entity;
import java.time.Instant;
public record LiberacaoProva(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,boolean tokenConfigurado,String tokenCifrado) {
 public LiberacaoProva(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm){this(id,titulo,materiaId,materia,disponivelEm,encerraEm,60,true,null);}
}
