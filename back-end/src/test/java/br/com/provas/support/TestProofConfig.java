package br.com.provas.support;
import br.com.provas.model.request.*;import java.math.BigDecimal;import java.util.List;
public final class TestProofConfig {
 public static ConfiguracaoProvaRequest config(List<Long> ids){return new ConfiguracaoProvaRequest(BigDecimal.TEN,10,ids.stream().map(id->new ConfiguracaoProvaRequest.Assunto(id,List.of(new BigDecimal("0.5"),BigDecimal.ONE,new BigDecimal("1.5"),new BigDecimal("2.0")))).toList());}
 public static List<ComposicaoProvaRequest> plan(List<Long> ids){if(ids.isEmpty())return List.of();return List.of(new ComposicaoProvaRequest(ids.getFirst(),new BigDecimal("0.5"),4),new ComposicaoProvaRequest(ids.getFirst(),BigDecimal.ONE,3),new ComposicaoProvaRequest(ids.getFirst(),new BigDecimal("1.5"),2),new ComposicaoProvaRequest(ids.getFirst(),new BigDecimal("2.0"),1));}
}
