package br.com.provas.model.entity;

import java.math.BigDecimal;

public record QuestaoResumo(Long id, String materia, String ensino, Integer ano, String letras, String abrangencia,
    String tipo, BigDecimal peso, String enunciado) {}
