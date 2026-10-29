package br.com.provas.model.entity;
import java.time.Instant;
public record ConfiguracaoInicioProva(Long id,Long professorId,Long materiaId,Long turmaId,String ensino,Integer ano,String titulo,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String tokenHash, java.util.List<br.com.provas.model.request.ComposicaoProvaRequest> composicao,java.math.BigDecimal valorTotal,Integer quantidadeQuestoes) {
 public ConfiguracaoInicioProva(Long id,Long professorId,Long materiaId,Long turmaId,String ensino,Integer ano,String titulo,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String tokenHash){this(id,professorId,materiaId,turmaId,ensino,ano,titulo,materia,disponivelEm,encerraEm,duracaoMinutos,tokenHash,java.util.List.of(),java.math.BigDecimal.TEN,10);}
}
