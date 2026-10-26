package br.com.provas.model.entity;
import br.com.provas.model.response.TentativaProvaResponse;
import java.time.Instant;
import java.util.Map;
public record EstadoProvaRedis(Long tentativaId,TentativaProvaResponse copia,Map<Integer,String> respostas,Map<Integer,Long> revisoes,boolean selada,boolean persistida,Instant enviadaEm,boolean automatica) {}
