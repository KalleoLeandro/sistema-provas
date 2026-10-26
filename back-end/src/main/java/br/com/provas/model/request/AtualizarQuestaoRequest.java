package br.com.provas.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtualizarQuestaoRequest(
    @NotNull(message = "Informe a versão carregada da questão.") @Positive(message = "Informe uma versão válida.") Long versaoId,
    @NotNull(message = "Informe a revisão carregada.") @Positive(message = "Informe uma revisão válida.") Long revisao,
    @NotNull(message = "Os dados da questão são obrigatórios.") @Valid QuestaoRequest questao
) {}
