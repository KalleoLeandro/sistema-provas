package br.com.provas.model.request;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
    @NotNull(message = "Informe se o perfil está ativo ou inativo.") Boolean ativo
) {}
