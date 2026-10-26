package br.com.provas.model.response;
import java.math.BigDecimal;
import java.util.Map;
public record QuestaoExecucaoResponse(int numero,String tipo,BigDecimal peso,String enunciado,Map<String,String> alternativas,String resposta,long revisao) {}
