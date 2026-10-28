package br.com.provas.model.response;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

public record QuestaoResponse(Long id, Long materiaId, String ensino, Integer ano, String abrangencia, List<TurmaResponse> turmas, String tipo, BigDecimal peso,
    String enunciado, Map<String, String> alternativas, String respostaCorreta, Long versaoId, Long revisao, Long assuntoId, String assunto) {
 public QuestaoResponse(Long id, Long materiaId, String ensino, Integer ano, String abrangencia, List<TurmaResponse> turmas, String tipo, BigDecimal peso,
    String enunciado, Map<String, String> alternativas, String respostaCorreta, Long versaoId, Long revisao){this(id,materiaId,ensino,ano,abrangencia,turmas,tipo,peso,enunciado,alternativas,respostaCorreta,versaoId,revisao,null,null);}
}
