package br.com.provas.model.response;

import java.math.BigDecimal;

public record QuestaoResumoResponse(Long id, String materia, String ensino, Integer ano, String letras, String abrangencia,
    String tipo, BigDecimal peso, String resumo) {}
