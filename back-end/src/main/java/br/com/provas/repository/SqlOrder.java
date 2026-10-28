package br.com.provas.repository;
import br.com.provas.model.request.OrdenacaoRequest;
import br.com.provas.exceptions.DefaultErrorException;
import java.util.Map;
/** Resolve exclusivamente expressões SQL constantes fornecidas pelo repository. */
final class SqlOrder {
 private SqlOrder(){}
 static String clause(OrdenacaoRequest order,Map<String,String> columns,String id){
  String expression=columns.get(order.ordenacao());if(expression==null)throw new DefaultErrorException("A coluna selecionada não está disponível nesta lista.");
  String direction="DESC".equals(order.direcao())?" DESC":" ASC";
  return " ORDER BY "+String.join(direction+",",expression.split(";"))+direction+","+id+direction;
 }
}
