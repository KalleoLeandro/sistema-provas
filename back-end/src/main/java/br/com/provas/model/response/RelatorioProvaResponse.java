package br.com.provas.model.response;

import java.math.BigDecimal;
import java.time.Instant;

public record RelatorioProvaResponse(Long id, String titulo, String materia, String professor,
        Long turmaId, Instant disponivelEm, Instant encerraEm) {
    public record Resultado(Long alunoId, String nome, Integer numero, String cgm, String status,
            Long tentativaId, Instant iniciadaEm, Instant entregueEm, Integer pendentes, BigDecimal nota) {}
}
