package br.com.provas.model.response;
import java.time.Instant;
public record RegrasProvaResponse(Long id,String titulo,String materia,int duracaoMinutos,Instant encerraEm,boolean iniciada,boolean submetida, Integer quantidadeQuestoes,java.math.BigDecimal valorTotal) {
 public RegrasProvaResponse(Long id,String titulo,String materia,int duracaoMinutos,Instant encerraEm,boolean iniciada,boolean submetida){this(id,titulo,materia,duracaoMinutos,encerraEm,iniciada,submetida,10,java.math.BigDecimal.TEN);}
}
