package br.com.provas.model.response;
import java.math.BigDecimal;import java.util.List;
public record PlanejamentoProvaResponse(boolean possivel,String motivo,Integer faltantes,List<Item> composicao){
 public record Item(Long assuntoId,String assunto,BigDecimal peso,int quantidade,int disponiveis,int faltantes){}
}
