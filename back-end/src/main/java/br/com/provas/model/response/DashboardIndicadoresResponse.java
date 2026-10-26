package br.com.provas.model.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardIndicadoresResponse(long alunosFizeram, long alunosPendentes, BigDecimal mediaGeral,
        long naoIniciadas, long emAndamento, long aguardandoCorrecao, long corrigidas,
        List<MediaTurma> mediasTurmas) {
    public record MediaTurma(Long turmaId, String ensino, Integer ano, String letra, BigDecimal media, long corrigidas) {}
}
