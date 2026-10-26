package br.com.provas.model.response;

import java.math.BigDecimal;
public record QuantidadeQuestoesResponse(BigDecimal peso, int minimo, int cadastradas, int faltantes) {}
