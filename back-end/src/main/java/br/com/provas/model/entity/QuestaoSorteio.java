package br.com.provas.model.entity;
import java.math.BigDecimal;
import java.util.Map;
public record QuestaoSorteio(Long id,Long versaoId,String tipo,BigDecimal peso,String enunciado,Map<String,String> alternativas,String respostaCorreta) {}
