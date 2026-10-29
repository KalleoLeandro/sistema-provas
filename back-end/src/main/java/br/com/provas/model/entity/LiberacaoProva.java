package br.com.provas.model.entity;
import java.time.Instant;
public record LiberacaoProva(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,boolean tokenConfigurado,String tokenCifrado, br.com.provas.model.request.ConfiguracaoProvaRequest configuracao,java.util.List<br.com.provas.model.request.ComposicaoProvaRequest> composicao,java.math.BigDecimal valorTotal,Integer quantidadeQuestoes) {
 public LiberacaoProva(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,boolean tokenConfigurado,String tokenCifrado){this(id,titulo,materiaId,materia,disponivelEm,encerraEm,duracaoMinutos,tokenConfigurado,tokenCifrado,null,java.util.List.of(),java.math.BigDecimal.TEN,10);}

 public LiberacaoProva(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm){this(id,titulo,materiaId,materia,disponivelEm,encerraEm,60,true,null);}
}
