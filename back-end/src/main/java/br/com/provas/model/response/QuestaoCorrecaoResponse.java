package br.com.provas.model.response;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
public record QuestaoCorrecaoResponse(int numero,String tipo,BigDecimal peso,String enunciado,Map<String,String> alternativas,String respostaCorreta,String resposta,BigDecimal pontuacao,String feedback,long revisao,Instant corrigidaEm) {}
