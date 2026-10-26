package br.com.provas.model.entity;

import java.math.BigDecimal;
import java.util.Map;

public record Questao(Long id, Long professorId, Long materiaId, String ensino, Integer ano, String abrangencia, String tipo, BigDecimal peso,
    String enunciado, Map<String, String> alternativas, String respostaCorreta) {}
